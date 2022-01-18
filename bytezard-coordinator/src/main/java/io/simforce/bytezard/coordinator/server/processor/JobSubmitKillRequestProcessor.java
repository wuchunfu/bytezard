package io.simforce.bytezard.coordinator.server.processor;

import io.simforce.bytezard.coordinator.server.cache.JobExecuteManager;
import io.simforce.bytezard.remote.command.Command;
import io.simforce.bytezard.remote.command.CommandCode;
import io.simforce.bytezard.remote.command.JobSubmitAckCommand;
import io.simforce.bytezard.remote.command.JobSubmitKillRequestCommand;
import io.simforce.bytezard.remote.processor.NettyEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Preconditions;

import io.netty.channel.Channel;
import io.simforce.bytezard.remote.utils.JsonSerializer;

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
