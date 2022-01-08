package io.simforce.bytezard.remote.connection;

import io.simforce.bytezard.remote.command.Command;
import io.simforce.bytezard.remote.utils.ChannelUtils;
import io.simforce.bytezard.remote.utils.Host;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

/**
 * @author zixi0825
 */
public class Connection {

    private Channel channel;

    private Host host;

    public Connection(Channel channel) {
        this.channel = channel;
        this.host =  ChannelUtils.toAddress(channel);
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

    public boolean isActive() {
        return channel !=null && channel.isActive();
    }

    public ChannelFuture writeAndFlush(Command command) {
        return this.channel.writeAndFlush(command);
    }

    public void close() {
        this.channel.close();
    }
}
