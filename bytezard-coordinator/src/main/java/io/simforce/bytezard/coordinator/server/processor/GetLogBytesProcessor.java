package io.simforce.bytezard.coordinator.server.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;
import io.simforce.bytezard.coordinator.server.log.LogService;
import io.simforce.bytezard.remote.command.Command;
import io.simforce.bytezard.remote.command.CommandCode;
import io.simforce.bytezard.remote.command.log.GetLogBytesRequestCommand;
import io.simforce.bytezard.remote.command.log.GetLogBytesResponseCommand;
import io.simforce.bytezard.remote.processor.NettyEventProcessor;
import io.simforce.bytezard.remote.utils.JsonSerializer;

public class GetLogBytesProcessor implements NettyEventProcessor {

    private final Logger logger = LoggerFactory.getLogger(GetLogBytesProcessor.class);

    private final LogService logService;

    public GetLogBytesProcessor(LogService logService){
        this.logService = logService;
    }

    @Override
    public void process(Channel channel, Command command) {

        Preconditions.checkArgument(CommandCode.GET_LOG_BYTES_REQUEST == command.getCode(),
                String.format("invalid command type : %s", command.getCode()));

        GetLogBytesRequestCommand getLogBytesRequestCommand =
                JsonSerializer.deserialize(command.getBody(),GetLogBytesRequestCommand.class);

        GetLogBytesResponseCommand getLogBytesResponseCommand = new GetLogBytesResponseCommand();
        getLogBytesResponseCommand.setMsg(logService.getLogBytes(
                getLogBytesRequestCommand.getTaskId()));
        channel.writeAndFlush(getLogBytesResponseCommand.convert2Command(command.getOpaque()));
    }

}
