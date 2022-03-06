package io.datavines.remote;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import io.datavines.remote.codec.MessageDecoder;
import io.datavines.remote.codec.MessageEncoder;
import io.datavines.remote.command.CommandCode;
import io.datavines.remote.config.NettyServerConfig;
import io.datavines.remote.handler.NettyServerHandler;
import io.datavines.remote.listener.HandlerListener;
import io.datavines.remote.processor.NettyEventProcessor;
import io.datavines.remote.utils.Constants;
import io.datavines.remote.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author zixi0825
 */
public class DataVinesRemoteServer {

    private final Logger logger = LoggerFactory.getLogger(DataVinesRemoteServer.class);

    private final ServerBootstrap serverBootstrap = new ServerBootstrap();

    private final NettyServerConfig serverConfig;

    private final MessageEncoder encoder = new MessageEncoder();

    private final NioEventLoopGroup parentGroup;

    private final NioEventLoopGroup childGroup;

    private final NettyServerHandler serverHandler;

    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    public DataVinesRemoteServer(NettyServerConfig serverConfig){
        this.serverConfig = serverConfig;
        this.parentGroup = new NioEventLoopGroup(1,new NamedThreadFactory("netty-server-parent"));
        this.childGroup = new NioEventLoopGroup(Constants.CPUS, new NamedThreadFactory("netty-server-child"));
        this.serverHandler = new NettyServerHandler(null);
    }

    public void start(){

        if(!isStarted.get()){
            this.serverBootstrap
                .group(parentGroup,childGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_BACKLOG,serverConfig.getSoBacklog())
                .childOption(ChannelOption.TCP_NODELAY,serverConfig.isTcpNoDelay())
                .childOption(ChannelOption.SO_KEEPALIVE,serverConfig.isSoKeepalive())
                .childOption(ChannelOption.SO_SNDBUF,serverConfig.getSendBufferSize())
                .childOption(ChannelOption.SO_RCVBUF,serverConfig.getReceiveBufferSize())
                .childHandler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        initNettyChannel(ch);
                    }
                });
            ChannelFuture future;

            try{
                future = serverBootstrap.bind(serverConfig.getListenPort()).sync();
            }catch(Exception e){
                logger.error("MangerRemoteServer bind fail {}, exit",e.getMessage(), e);
                throw new RuntimeException(String.format("MangerRemoteServer bind %s fail", serverConfig.getListenPort()));
            }

            if(future.isSuccess()){
                logger.info("MangerRemoteServer bind success at port :{}",serverConfig.getListenPort());
            }else if(future.cause() != null){
                throw new RuntimeException(String.format("MangerRemoteServer bind %s fail",serverConfig.getListenPort()), future.cause());
            }else {
                throw new RuntimeException(String.format("MangerRemoteServer bind %s fail", serverConfig.getListenPort()));
            }

            isStarted.compareAndSet(false,true);
        }

    }

    /**
     *  init netty channel
     * @param ch socket channel
     * @throws Exception
     */
    private void initNettyChannel(NioSocketChannel ch) throws Exception{
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("encoder", encoder);
        pipeline.addLast("decoder", new MessageDecoder());
        pipeline.addLast("handler", serverHandler);
    }

    public void registerProcessor(CommandCode commandCode, NettyEventProcessor processor){
        this.registerProcessor(commandCode,processor,null);
    }

    public void registerProcessor(CommandCode commandCode, NettyEventProcessor processor, ExecutorService executorService){
        this.serverHandler.registerProcessor(commandCode,processor,executorService);
    }

    public void close(){
        if(isStarted.compareAndSet(true,false)){
            try{
                if(parentGroup != null){
                    parentGroup.shutdownGracefully();
                }

                if(childGroup != null){
                    childGroup.shutdownGracefully();
                }
            }catch (Exception e){
                logger.error("netty server close exception", e);
            }

            logger.info("netty server closed");
        }
    }

    public void addListener(HandlerListener handlerListener){
        this.serverHandler.addListener(handlerListener);
    }

    public void removeListener(HandlerListener handlerListener){
        this.serverHandler.removeListener(handlerListener);
    }
}
