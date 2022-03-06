package io.datavines.coordinator.server.processor;

import io.datavines.coordinator.server.channel.ClientChannel;
import io.datavines.coordinator.server.channel.ClientChannelManager;
import io.datavines.common.utils.JSONUtils;
import io.datavines.remote.command.Command;
import io.datavines.remote.command.CommandCode;
import io.datavines.remote.command.PingCommand;
import io.datavines.remote.command.PongCommand;
import io.datavines.remote.processor.NettyEventProcessor;
import io.datavines.remote.utils.ChannelUtils;
import io.datavines.remote.utils.JsonSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

public class PingProcessor implements NettyEventProcessor {

    private final Logger logger = LoggerFactory.getLogger(PingProcessor.class);

    private final ClientChannelManager clientChannelManager;

    public PingProcessor(){
        clientChannelManager = ClientChannelManager.getInstance();
    }

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(
                CommandCode.PING == command.getCode(),
                String.format("invalid command type : %s", command.getCode()));

        PingCommand pingCommand =
                JsonSerializer.deserialize(command.getBody(),PingCommand.class);

        logger.info(JSONUtils.toJsonString(pingCommand));

        ClientChannel clientChannel = new ClientChannel();
        clientChannel.setChannel(channel);
        clientChannel.setHost(ChannelUtils.toAddress(channel));

        clientChannelManager
                .addClientChannel(pingCommand.getClientType(),clientChannel);

        PongCommand pongCommand = new PongCommand();
        pongCommand.setClientType(pingCommand.getClientType());

        channel.writeAndFlush(pongCommand.convert2Command());
    }
}
