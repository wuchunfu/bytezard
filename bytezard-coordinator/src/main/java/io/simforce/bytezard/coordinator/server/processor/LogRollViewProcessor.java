package io.simforce.bytezard.coordinator.server.processor;

import io.simforce.bytezard.common.entity.LogResult;
import io.simforce.bytezard.coordinator.server.log.LogService;
import io.simforce.bytezard.remote.command.Command;
import io.simforce.bytezard.remote.command.CommandCode;
import io.simforce.bytezard.remote.command.log.RollViewLogRequestCommand;
import io.simforce.bytezard.remote.command.log.RollViewLogResponseCommand;
import io.simforce.bytezard.remote.processor.NettyEventProcessor;
import io.simforce.bytezard.remote.utils.FastJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

/**
 * @author zixi0825
 */
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
                FastJsonSerializer.deserialize(command.getBody(),RollViewLogRequestCommand.class);
        LogResult logResult = logService.queryLog(
                rollViewLogRequestCommand.getJobInstanceId(),
                rollViewLogRequestCommand.getSkipLineNum(),
                rollViewLogRequestCommand.getLimit());

        RollViewLogResponseCommand rollViewLogResponseCommand = new RollViewLogResponseCommand();
        rollViewLogResponseCommand.setMsg(logResult.getMsg());
        rollViewLogResponseCommand.setOffsetLine(logResult.getOffsetLine());

        channel.writeAndFlush(rollViewLogResponseCommand.convert2Command(command.getOpaque()));
    }

}
