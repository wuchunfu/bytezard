package io.simforce.bytezard.remote;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.simforce.bytezard.remote.codec.MessageDecoder;
import io.simforce.bytezard.remote.codec.MessageEncoder;
import io.simforce.bytezard.remote.command.Command;
import io.simforce.bytezard.remote.command.CommandCode;
import io.simforce.bytezard.remote.command.RequestClientType;
import io.simforce.bytezard.remote.config.NettyClientConfig;
import io.simforce.bytezard.remote.exception.RemotingException;
import io.simforce.bytezard.remote.exception.RemotingTimeoutException;
import io.simforce.bytezard.remote.future.ResponseFuture;
import io.simforce.bytezard.remote.future.ResponseFutureManager;
import io.simforce.bytezard.remote.handler.NettyClientHandler;
import io.simforce.bytezard.remote.listener.HandlerListener;
import io.simforce.bytezard.remote.manager.ChannelManager;
import io.simforce.bytezard.remote.processor.NettyEventProcessor;
import io.simforce.bytezard.remote.utils.Constants;
import io.simforce.bytezard.remote.utils.Host;
import io.simforce.bytezard.remote.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author zixi0825
 */
public class BytezardRemoteClient {

    private final Logger logger = LoggerFactory.getLogger(BytezardRemoteClient.class);

    protected final Bootstrap bootstrap = new Bootstrap();

    private final MessageEncoder encoder = new MessageEncoder();

    protected NettyClientConfig clientConfig;

    protected final ChannelManager channelManager = new ChannelManager();

    protected final AtomicBoolean isStarted = new AtomicBoolean(false);

    private NioEventLoopGroup workerGroup;

    /**
     *  callback thread executor
     */
    private ExecutorService callbackExecutor;

    /**
     *  client handler
     */
    private NettyClientHandler clientHandler;

    /**
     *  response future executor
     */
    private ScheduledExecutorService responseFutureExecutor;

    private RequestClientType clientType = RequestClientType.EXECUTOR;

    public BytezardRemoteClient(NettyClientConfig clientConfig){

        this.clientConfig = clientConfig;
        workerGroup = new NioEventLoopGroup(Constants.CPUS,new NamedThreadFactory("netty-client-worker"));
        clientHandler = new NettyClientHandler(channelManager,callbackExecutor,this);
        this.start();
    }

    public void send(Host host,Command command) throws RemotingException{
        Channel channel = getChannel(host);
        if(channel == null){
            throw new RemotingException(String.format("connect to : %s fail", host));
        }

        try{
            ChannelFuture future = channel.writeAndFlush(command).await();
            if(future.isSuccess()){
                logger.debug("send command : {} , to : {} successfully.", command, host.getAddress());
            }else{
                String msg = String.format("send command : %s , to :%s failed", command, host.getAddress());
                logger.error(msg, future.cause());
                throw new RemotingException(msg);
            }
        }catch (Exception e){
            logger.error("Send command {} to address {} encounter error.", command, host.getAddress());
            throw new RemotingException(String.format("Send command : %s , to :%s encounter error", command, host.getAddress()), e);
        }
    }

    /**
     * sync send
     * @param host host
     * @param command command
     * @param timeoutMillis timeoutMillis
     * @return command
     * @throws InterruptedException
     * @throws RemotingException
     */
    public Command sendSync(final Host host, final Command command, final long timeoutMillis) throws InterruptedException, RemotingException {
        final Channel channel = getChannel(host);
        if (channel == null) {
            throw new RemotingException(String.format("connect to : %s fail", host));
        }
        final long opaque = command.getOpaque();
        final ResponseFuture responseFuture = new ResponseFuture(opaque, timeoutMillis, null, null);
        ResponseFutureManager.putResponseFuture(opaque,responseFuture);
        channel.writeAndFlush(command).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(future.isSuccess()){
                    responseFuture.setSendSuccess(true);
                    return;
                } else {
                    responseFuture.setSendSuccess(false);
                }
                responseFuture.setCause(future.cause());
                responseFuture.putResponse(null);
                ResponseFutureManager.removeResponseFuture(opaque);
                logger.error("send command {} to host {} failed", command, host);
            }
        });
        /**
         * sync wait for result
         */
        Command result = responseFuture.waitResponse();
        if(result == null){
            if(responseFuture.isSendSuccess()){
                throw new RemotingTimeoutException(host.toString(), timeoutMillis, responseFuture.getCause());
            } else{
                throw new RemotingException(host.toString(), responseFuture.getCause());
            }
        }
        return result;
    }


    public void registerProcessor(CommandCode commandCode, NettyEventProcessor processor){
        this.registerProcessor(commandCode,processor,null);
    }

    public void registerProcessor(CommandCode commandCode, NettyEventProcessor processor, ExecutorService executorService){
        this.clientHandler.registerProcessor(commandCode,processor,executorService);
    }

    public Channel getChannel(Host host){
        Channel channel = channelManager.getChannel(host);
        if(channel != null && channel.isActive()){
            return channel;
        }

        return doConnect(host,true);
    }

    private Channel getChannel(Host host, ChannelFuture future) {
        Channel channel = future.channel();
        channelManager.putChannel(host,channel);
        logger.info("connect to {} success", host);
        return channel;
    }

    /**
     *  start
     */
    private void start(){

        this.bootstrap
                .group(this.workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, clientConfig.isSoKeepalive())
                .option(ChannelOption.TCP_NODELAY, clientConfig.isTcpNoDelay())
                .option(ChannelOption.SO_SNDBUF, clientConfig.getSendBufferSize())
                .option(ChannelOption.SO_RCVBUF, clientConfig.getReceiveBufferSize())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new IdleStateHandler(30, 0, 0));
                        ch.pipeline().addLast(
                                new MessageDecoder(),
                                clientHandler,
                                encoder);
                    }
                });

        isStarted.compareAndSet(false, true);
    }

    public void close(){
        if(isStarted.compareAndSet(true,false)){
            try{
                channelManager.closeChannels();
                if(workerGroup != null){
                    workerGroup.shutdownGracefully();
                }
                if(callbackExecutor != null){
                    callbackExecutor.shutdownNow();
                }
            }catch (Exception e){
                logger.error("netty client close exception",e);
            }

            logger.info("netty client closed");
        }
    }

    public Channel doConnect(Host host,boolean isSync){

        ChannelFuture future;
        try{

            synchronized (bootstrap){
                future = bootstrap.connect(
                        new InetSocketAddress(host.getIp(),host.getPort()));
            }

            if(isSync){
                future.sync();
            }

            if(future.isSuccess()){
                return getChannel(host, future);
            }

        }catch (Exception e){
            logger.info("connect to {} error  {}", host, e);
        }

        return null;
    }

    public void addListener(HandlerListener handlerListener){
        this.clientHandler.addListener(handlerListener);
    }

    public void removeListener(HandlerListener handlerListener){
        this.clientHandler.removeListener(handlerListener);
    }

    public void setClientType(RequestClientType clientType){
        this.clientType = clientType;
    }

    public RequestClientType getClientType() {
        return clientType;
    }
}
