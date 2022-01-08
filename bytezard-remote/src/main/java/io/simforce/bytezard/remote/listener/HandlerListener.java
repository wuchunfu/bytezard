package io.simforce.bytezard.remote.listener;

import io.netty.channel.ChannelHandlerContext;
import io.simforce.bytezard.remote.BytezardRemoteClient;

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
    void channelInactive(ChannelHandlerContext ctx, BytezardRemoteClient bytezardRemoteClient) throws Exception;
}
