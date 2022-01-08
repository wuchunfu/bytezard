package io.simforce.bytezard.coordinator.server.processor;

import io.simforce.bytezard.common.entity.LogResult;
import io.simforce.bytezard.coordinator.server.log.LogService;
import io.simforce.bytezard.remote.command.Command;
import io.simforce.bytezard.remote.command.CommandCode;
import io.simforce.bytezard.remote.command.log.ViewLogRequestCommand;
import io.simforce.bytezard.remote.command.log.ViewLogResponseCommand;
import io.simforce.bytezard.remote.processor.NettyEventProcessor;
import io.simforce.bytezard.remote.utils.FastJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

/**
 * @author zixi0825
 */
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
                FastJsonSerializer.deserialize(command.getBody(),ViewLogRequestCommand.class);

        LogResult logResult = logService.queryWholeLog(
                rollViewLogRequestCommand.getJobInstanceId());

        ViewLogResponseCommand viewLogResponseCommand = new ViewLogResponseCommand();
        viewLogResponseCommand.setMsg(logResult.getMsg());
        channel.writeAndFlush(viewLogResponseCommand.convert2Command(command.getOpaque()));
    }

}
