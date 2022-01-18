package io.simforce.bytezard.executor.processor;

import io.simforce.bytezard.common.utils.JSONUtils;
import io.simforce.bytezard.remote.command.Command;
import io.simforce.bytezard.remote.command.CommandCode;
import io.simforce.bytezard.remote.command.PongCommand;
import io.simforce.bytezard.remote.processor.NettyEventProcessor;
import io.simforce.bytezard.remote.utils.JsonSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

public class PongProcessor implements NettyEventProcessor {

    private final Logger logger = LoggerFactory.getLogger(PongProcessor.class);

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(
                CommandCode.PONG == command.getCode(),
                String.format("invalid command type : %s", command.getCode()));

        PongCommand pongCommand =
                JsonSerializer.deserialize(command.getBody(),PongCommand.class);

        logger.info(JSONUtils.toJsonString(pongCommand));
    }
}
