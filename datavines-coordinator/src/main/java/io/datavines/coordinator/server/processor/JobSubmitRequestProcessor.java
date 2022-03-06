package io.datavines.coordinator.server.processor;

import io.datavines.coordinator.server.cache.JobExecuteManager;
import io.datavines.common.entity.TaskRequest;
import io.datavines.remote.command.Command;
import io.datavines.remote.command.CommandCode;
import io.datavines.remote.command.JobSubmitAckCommand;
import io.datavines.remote.command.JobSubmitRequestCommand;
import io.datavines.remote.processor.NettyEventProcessor;
import io.datavines.remote.utils.ChannelUtils;
import io.datavines.remote.utils.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

public class JobSubmitRequestProcessor implements NettyEventProcessor {

    private final Logger logger = LoggerFactory.getLogger(JobSubmitRequestProcessor.class);

    private final JobExecuteManager jobExecuteManager;

    public JobSubmitRequestProcessor(JobExecuteManager jobExecuteManager){
        this.jobExecuteManager = jobExecuteManager;
    }

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(
                CommandCode.JOB_SUBMIT_REQUEST == command.getCode(),
                String.format("invalid command type : %s", command.getCode()));
        JobSubmitRequestCommand jobSubmitRequestCommand = JsonSerializer.deserialize(new String(command.getBody()), JobSubmitRequestCommand.class);

        JobSubmitAckCommand jobSubmitAckCommand = new JobSubmitAckCommand();

        if(jobSubmitRequestCommand == null){
            jobSubmitAckCommand.setCode(0);
            jobSubmitAckCommand.setMsg("command is null");
            channel.writeAndFlush(jobSubmitAckCommand.convert2Command(command.getOpaque()));
            return;
        }

        TaskRequest taskRequest = JsonSerializer.deserialize(jobSubmitRequestCommand.getJobExecutionContext(), TaskRequest.class);

        if(taskRequest == null){
            jobSubmitAckCommand.setCode(0);
            jobSubmitAckCommand.setMsg("execution job is null");
            channel.writeAndFlush(jobSubmitAckCommand.convert2Command(command.getOpaque()));
            return;
        }

//        taskRequest.setTaskId(id);
        jobExecuteManager.addExecuteCommand(taskRequest);
        jobExecuteManager.putTaskId2ClientIpMap(
                taskRequest.getTaskId(),ChannelUtils.toAddress(channel).getIp());

//        jobSubmitAckCommand.setTaskId(id);
        jobSubmitAckCommand.setCode(1);
        jobSubmitAckCommand.setMsg("submit job success");
        channel.writeAndFlush(jobSubmitAckCommand.convert2Command(command.getOpaque()));
    }
}
