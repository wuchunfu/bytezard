package io.simforce.bytezard.remote.manager;

import java.util.concurrent.ConcurrentHashMap;

import io.simforce.bytezard.remote.utils.Host;

import io.netty.channel.Channel;

/**
 * @author zixi0825
 */
public class ChannelManager {

    /**
     * channels
     */
    private final ConcurrentHashMap<Host, Channel> channels = new ConcurrentHashMap<>(128);

    /**
     *  close channels
     */
    public void closeChannels(){
        for (Channel channel : this.channels.values()) {
            channel.close();
        }
        this.channels.clear();
    }

    /**
     * close channel
     * @param host host
     */
    public void closeChannel(Host host){
        Channel channel = this.channels.remove(host);
        if(channel != null){
            channel.close();
        }
    }

    /**
     *
     * @param host
     * @return
     */
    public Channel getChannel(Host host) {
        Channel channel = channels.get(host);
        if(channel != null && channel.isActive()){
            return channel;
        }

        return null;
    }

    public void putChannel(Host host, Channel channel){
        channels.put(host,channel);
    }
}
