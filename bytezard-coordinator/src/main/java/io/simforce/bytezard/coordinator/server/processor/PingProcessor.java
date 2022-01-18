package io.simforce.bytezard.coordinator.server.processor;

import io.simforce.bytezard.coordinator.server.channel.ClientChannel;
import io.simforce.bytezard.coordinator.server.channel.ClientChannelManager;
import io.simforce.bytezard.remote.command.Command;
import io.simforce.bytezard.remote.command.CommandCode;
import io.simforce.bytezard.remote.command.PingCommand;
import io.simforce.bytezard.remote.command.PongCommand;
import io.simforce.bytezard.remote.processor.NettyEventProcessor;
import io.simforce.bytezard.remote.utils.ChannelUtils;
import io.simforce.bytezard.remote.utils.JsonSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.support.json.JSONUtils;
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

        logger.info(JSONUtils.toJSONString(pingCommand));

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
