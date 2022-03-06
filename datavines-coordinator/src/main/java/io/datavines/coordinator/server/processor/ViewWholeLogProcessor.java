package io.datavines.coordinator.server.processor;

import io.datavines.coordinator.server.log.LogService;
import io.datavines.common.entity.LogResult;
import io.datavines.remote.command.Command;
import io.datavines.remote.command.CommandCode;
import io.datavines.remote.command.log.ViewLogRequestCommand;
import io.datavines.remote.command.log.ViewLogResponseCommand;
import io.datavines.remote.processor.NettyEventProcessor;
import io.datavines.remote.utils.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

public class ViewWholeLogProcessor implements NettyEventProcessor {

    private final Logger logger = LoggerFactory.getLogger(ViewWholeLogProcessor.class);

    private final LogService logService;

    public ViewWholeLogProcessor(LogService logService){
        this.logService = logService;
    }

    @Override
    public void process(Channel channel, Command command) {

        Preconditions.checkArgument(CommandCode.VIEW_WHOLE_LOG_REQUEST == command.getCode(),
                String.format("invalid command type : %s", command.getCode()));

        ViewLogRequestCommand rollViewLogRequestCommand =
                JsonSerializer.deserialize(command.getBody(),ViewLogRequestCommand.class);

        LogResult logResult = logService.queryWholeLog(
                rollViewLogRequestCommand.getTaskId());

        ViewLogResponseCommand viewLogResponseCommand = new ViewLogResponseCommand();
        viewLogResponseCommand.setMsg(logResult.getMsg());
        channel.writeAndFlush(viewLogResponseCommand.convert2Command(command.getOpaque()));
    }

}
