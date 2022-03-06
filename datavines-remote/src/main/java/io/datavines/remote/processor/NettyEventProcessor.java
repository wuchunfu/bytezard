package io.datavines.remote.processor;

import io.datavines.remote.command.Command;

import io.netty.channel.Channel;

/**
 * @author zixi0825
 */
public interface NettyEventProcessor {

    /**
     * process command
     * @param channel netty channel
     * @param command command
     */
    void process(final Channel channel, final Command command);
}
