package io.datavines.executor.processor;

import io.datavines.common.utils.JSONUtils;
import io.datavines.remote.command.Command;
import io.datavines.remote.command.CommandCode;
import io.datavines.remote.command.PongCommand;
import io.datavines.remote.processor.NettyEventProcessor;
import io.datavines.remote.utils.JsonSerializer;

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
