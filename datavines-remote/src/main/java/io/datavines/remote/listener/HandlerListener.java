package io.datavines.remote.listener;

import io.datavines.remote.DataVinesRemoteClient;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author zixi0825
 */
public interface HandlerListener {

    /**
     * channel active
     * @param ctx
     * @throws Exception
     */
    void channelActive(ChannelHandlerContext ctx) throws Exception;

    /**
     * channel inactive
     * @param ctx
     * @throws Exception
     */
    void channelInactive(ChannelHandlerContext ctx) throws Exception;

    /**
     * channel inactive
     * @param ctx
     * @throws Exception
     */
    void channelInactive(ChannelHandlerContext ctx, DataVinesRemoteClient datavinesRemoteClient) throws Exception;
}
