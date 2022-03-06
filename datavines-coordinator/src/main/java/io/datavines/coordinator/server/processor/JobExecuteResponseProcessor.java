package io.datavines.coordinator.server.processor;

import io.datavines.coordinator.server.cache.JobExecuteManager;
import io.datavines.common.entity.TaskRequest;
import io.datavines.common.utils.JSONUtils;
import io.datavines.remote.command.Command;
import io.datavines.remote.command.CommandCode;
import io.datavines.remote.command.JobExecuteResponseCommand;
import io.datavines.remote.processor.NettyEventProcessor;
import io.datavines.remote.utils.ChannelUtils;
import io.datavines.remote.utils.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

public class JobExecuteResponseProcessor implements NettyEventProcessor {

    private final Logger logger = LoggerFactory.getLogger(JobExecuteResponseProcessor.class);

    private final JobExecuteManager jobExecuteManager;

    public JobExecuteResponseProcessor(JobExecuteManager jobExecuteManager){
        this.jobExecuteManager = jobExecuteManager;
    }

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(
                CommandCode.JOB_EXECUTE_RESPONSE == command.getCode(),
                String.format("invalid command type : %s", command.getCode()));
        JobExecuteResponseCommand jobExecuteResponseCommand = JsonSerializer.deserialize(command.getBody(), JobExecuteResponseCommand.class);
        logger.info("jobExecuteResponseCommand : {}", JSONUtils.toJsonString(jobExecuteResponseCommand));

        TaskRequest taskRequest = jobExecuteManager.getExecutionJob(jobExecuteResponseCommand.getTaskId());
        if(taskRequest == null){
            taskRequest =  new TaskRequest();
        }

        taskRequest.setTaskId(jobExecuteResponseCommand.getTaskId());
        taskRequest.setEndTime(jobExecuteResponseCommand.getEndTime());
        taskRequest.setStatus(jobExecuteResponseCommand.getStatus());
        String workerAddress = ChannelUtils.toAddress(channel).getAddress();
        taskRequest.setExecuteHost(workerAddress);
        taskRequest.setApplicationId(jobExecuteResponseCommand.getApplicationIds());
        taskRequest.setProcessId(jobExecuteResponseCommand.getProcessId());

        JobResponseContext jobResponseContext = new JobResponseContext(CommandCode.JOB_EXECUTE_RESPONSE, taskRequest);

        jobExecuteManager.putResponse(jobResponseContext);
    }
}
