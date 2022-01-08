package io.simforce.bytezard.remote.listener;

import io.netty.channel.ChannelHandlerContext;
import io.simforce.bytezard.remote.BytezardRemoteClient;

/**
 * @author zixi0825
 */
public class BaseHandlerListener implements HandlerListener {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx, BytezardRemoteClient bytezardRemoteClient) throws Exception {

    }
}
