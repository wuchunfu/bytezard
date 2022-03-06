package io.datavines.coordinator.server.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.datavines.remote.command.Command;
import io.datavines.remote.exception.RemotingException;
import io.datavines.remote.exception.RemotingTimeoutException;
import io.datavines.remote.future.ResponseFuture;
import io.datavines.remote.future.ResponseFutureManager;
import io.datavines.remote.utils.ChannelUtils;
import io.datavines.remote.utils.Host;

public class ClientChannel {

    private static final Logger logger = LoggerFactory.getLogger(ClientChannel.class);

    private Channel channel;

    private Host host;

    private ClientWeight clientWeight;

    public ClientChannel(){}

    public ClientChannel(Channel channel, Host host, ClientWeight clientWeight) {
        this.channel = channel;
        this.host = host;
        this.clientWeight = clientWeight;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public ClientWeight getClientWeight() {
        return clientWeight;
    }

    public void setClientWeight(ClientWeight clientWeight) {
        this.clientWeight = clientWeight;
    }

    public Command sendSync(Command command, long timeoutMillis){
        try{
            Host host = ChannelUtils.toAddress(channel);
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
                    logger.error("send command {} to host {} failed", command, ChannelUtils.toAddress(channel));
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
        }catch (Exception e){
            logger.error("request log error {}",e);
        }

        return null;
    }
}
