package io.datavines.coordinator.server.processor;

import io.datavines.coordinator.server.cache.JobExecuteManager;
import io.datavines.common.entity.TaskRequest;
import io.datavines.common.utils.JSONUtils;
import io.datavines.remote.command.Command;
import io.datavines.remote.command.CommandCode;
import io.datavines.remote.command.JobExecuteAckCommand;
import io.datavines.remote.processor.NettyEventProcessor;
import io.datavines.remote.utils.ChannelUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;
import io.datavines.remote.utils.JsonSerializer;

public class JobAckProcessor implements NettyEventProcessor {

    private final Logger logger = LoggerFactory.getLogger(JobAckProcessor.class);

    private final JobExecuteManager jobExecuteManager;

    public JobAckProcessor(JobExecuteManager jobExecuteManager){
        this.jobExecuteManager = jobExecuteManager;
    }

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(
                CommandCode.JOB_EXECUTE_ACK == command.getCode(),
                String.format("invalid command type : %s", command.getCode()));
        JobExecuteAckCommand jobAckCommand = JsonSerializer.deserialize(new String(command.getBody()), JobExecuteAckCommand.class);
        logger.info(JSONUtils.toJsonString(jobAckCommand));

        TaskRequest taskRequest = jobExecuteManager.getExecutionJob(jobAckCommand.getTaskId());
        if(taskRequest == null){
            taskRequest =  new TaskRequest();
        }

        taskRequest.setTaskId(jobAckCommand.getTaskId());
        taskRequest.setStartTime(jobAckCommand.getStartTime());
        taskRequest.setStatus(jobAckCommand.getStatus());
        String workerAddress = ChannelUtils.toAddress(channel).getAddress();
        taskRequest.setExecuteHost(workerAddress);
        taskRequest.setLogPath(jobAckCommand.getLogPath());
        taskRequest.setExecuteFilePath(jobAckCommand.getExecutePath());

        JobResponseContext jobResponseContext = new JobResponseContext(CommandCode.JOB_EXECUTE_ACK, taskRequest);

        jobExecuteManager.putResponse(jobResponseContext);
    }
}
