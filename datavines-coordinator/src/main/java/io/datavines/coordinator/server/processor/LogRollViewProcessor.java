package io.datavines.coordinator.server.processor;

import io.datavines.coordinator.server.log.LogService;
import io.datavines.common.entity.LogResult;
import io.datavines.remote.command.Command;
import io.datavines.remote.command.CommandCode;
import io.datavines.remote.command.log.RollViewLogRequestCommand;
import io.datavines.remote.command.log.RollViewLogResponseCommand;
import io.datavines.remote.processor.NettyEventProcessor;
import io.datavines.remote.utils.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

public class LogRollViewProcessor implements NettyEventProcessor {

    private final Logger logger = LoggerFactory.getLogger(LogRollViewProcessor.class);

    private final LogService logService;

    public LogRollViewProcessor(LogService logService){
        this.logService = logService;
    }

    @Override
    public void process(Channel channel, Command command) {

        Preconditions.checkArgument(CommandCode.ROLL_VIEW_LOG_REQUEST == command.getCode(),
                String.format("invalid command type : %s", command.getCode()));

        RollViewLogRequestCommand rollViewLogRequestCommand =
                JsonSerializer.deserialize(command.getBody(),RollViewLogRequestCommand.class);
        LogResult logResult = logService.queryLog(
                rollViewLogRequestCommand.getTaskId(),
                rollViewLogRequestCommand.getSkipLineNum(),
                rollViewLogRequestCommand.getLimit());

        RollViewLogResponseCommand rollViewLogResponseCommand = new RollViewLogResponseCommand();
        rollViewLogResponseCommand.setMsg(logResult.getMsg());
        rollViewLogResponseCommand.setOffsetLine(logResult.getOffsetLine());

        channel.writeAndFlush(rollViewLogResponseCommand.convert2Command(command.getOpaque()));
    }

}
