package io.datavines.remote.listener;

import io.datavines.remote.DataVinesRemoteClient;
import io.netty.channel.ChannelHandlerContext;

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
    public void channelInactive(ChannelHandlerContext ctx, DataVinesRemoteClient datavinesRemoteClient) throws Exception {

    }
}
