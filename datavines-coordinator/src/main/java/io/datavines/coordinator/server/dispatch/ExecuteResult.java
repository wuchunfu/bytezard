package io.datavines.coordinator.server.dispatch;

import io.netty.channel.Channel;

/**
 * @author zixi0825
 */
public class ExecuteResult {

    private boolean result;

    private Channel channel;

    public ExecuteResult(boolean result, Channel channel){
        this.result = result;
        this.channel = channel;
    }

    public boolean getResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
