package io.simforce.bytezard.remote.processor;

import io.simforce.bytezard.remote.command.Command;

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
