package io.datavines.remote.handler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

import io.datavines.remote.DataVinesRemoteClient;
import io.datavines.remote.command.Command;
import io.datavines.remote.command.PingCommand;
import io.datavines.remote.listener.HandlerListener;
import io.datavines.remote.manager.ChannelManager;
import io.datavines.remote.utils.ChannelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @author zixi0825
 */
@ChannelHandler.Sharable
public class NettyClientHandler extends NettyBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    private final ChannelManager channelManager;

    private final DataVinesRemoteClient datavinesRemoteClient;

    private final List<HandlerListener> listeners = new CopyOnWriteArrayList<>();

    public NettyClientHandler(ChannelManager channelManager,
                              ExecutorService callbackExecutor,
                              DataVinesRemoteClient datavinesRemoteClient){
        this.channelManager = channelManager;
        this.callbackExecutor = callbackExecutor;
        this.datavinesRemoteClient = datavinesRemoteClient;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channelManager.closeChannel(ChannelUtils.toAddress(ctx.channel()));
        logger.info("{} channel close",ChannelUtils.toAddress(ctx.channel()));
        for(HandlerListener listener:listeners){
            listener.channelInactive(ctx, datavinesRemoteClient);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        processMsg(ctx.channel(),(Command)msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("exceptionCaught : {}", cause);
        channelManager.closeChannel(ChannelUtils.toAddress(ctx.channel()));
        ctx.channel().close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                //??????PingCommand
                PingCommand pingCommand = new PingCommand();
                pingCommand.setClientType(datavinesRemoteClient.getClientType());
                ctx.channel().writeAndFlush(pingCommand.convert2Command());
            }
        }
    }

    public void addListener(HandlerListener handlerListener){
        listeners.add(handlerListener);
    }

    public void removeListener(HandlerListener handlerListener){
        listeners.remove(handlerListener);
    }

}
