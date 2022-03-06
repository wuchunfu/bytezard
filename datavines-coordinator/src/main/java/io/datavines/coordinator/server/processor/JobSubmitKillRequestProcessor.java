package io.datavines.coordinator.server.processor;

import io.datavines.coordinator.server.cache.JobExecuteManager;
import io.datavines.remote.command.Command;
import io.datavines.remote.command.CommandCode;
import io.datavines.remote.command.JobSubmitAckCommand;
import io.datavines.remote.command.JobSubmitKillRequestCommand;
import io.datavines.remote.processor.NettyEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Preconditions;

import io.netty.channel.Channel;
import io.datavines.remote.utils.JsonSerializer;

public class JobSubmitKillRequestProcessor implements NettyEventProcessor {

    private final Logger logger = LoggerFactory.getLogger(JobSubmitKillRequestProcessor.class);

    private final JobExecuteManager jobExecuteManager;

    public JobSubmitKillRequestProcessor(JobExecuteManager jobExecuteManager){
        this.jobExecuteManager = jobExecuteManager;
    }

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(
                CommandCode.JOB_KILL_REQUEST == command.getCode(),
                String.format("invalid command type : %s", command.getCode()));
        JobSubmitKillRequestCommand jobSubmitKillRequestCommand =
                JsonSerializer.deserialize(new String(command.getBody()), JobSubmitKillRequestCommand.class);

        JobSubmitAckCommand jobSubmitKillAckCommand = new JobSubmitAckCommand();

        if(jobSubmitKillRequestCommand == null){
            jobSubmitKillAckCommand.setCode(0);
            jobSubmitKillAckCommand.setMsg("submit kill command is null");
            channel.writeAndFlush(jobSubmitKillAckCommand.convert2Command(command.getOpaque()));
            return;
        }

        jobExecuteManager.addKillCommand(jobSubmitKillRequestCommand.getTaskId());

        jobSubmitKillAckCommand.setCode(1);
        jobSubmitKillAckCommand.setMsg("submit kill job success");
        channel.writeAndFlush(jobSubmitKillAckCommand.convert2Command(command.getOpaque()));
    }
}
