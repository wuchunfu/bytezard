package io.datavines.remote.handler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

import io.datavines.remote.command.Command;
import io.datavines.remote.listener.HandlerListener;
import io.datavines.remote.utils.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zixi0825
 */
@ChannelHandler.Sharable
public class NettyServerHandler extends NettyBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private final List<HandlerListener> listeners = new CopyOnWriteArrayList<>();

    public NettyServerHandler(ExecutorService callbackExecutor){
        this.callbackExecutor = callbackExecutor;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info(ChannelUtils.toAddress(ctx.channel()).toString());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().close();
        for(HandlerListener listener:listeners){
            listener.channelInactive(ctx);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        processMsg(ctx.channel(),(Command)msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("exceptionCaught : {}", cause);
        ctx.channel().close();
    }

    /**
     *  channel write changed
     *
     * @param ctx  channel handler context
     * @throws Exception
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel ch = ctx.channel();
        ChannelConfig config = ch.config();

        if (!ch.isWritable()) {
            if (logger.isWarnEnabled()) {
                logger.warn("{} is not writable, over high water level : {}",
                        ch, config.getWriteBufferHighWaterMark());
            }

            config.setAutoRead(false);
        } else {
            if (logger.isWarnEnabled()) {
                logger.warn("{} is writable, to low water : {}",
                        ch, config.getWriteBufferLowWaterMark());
            }
            config.setAutoRead(true);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    public void addListener(HandlerListener handlerListener){
        listeners.add(handlerListener);
    }

    public void removeListener(HandlerListener handlerListener){
        listeners.remove(handlerListener);
    }
}
