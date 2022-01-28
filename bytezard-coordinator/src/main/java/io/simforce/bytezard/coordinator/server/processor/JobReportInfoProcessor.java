package io.simforce.bytezard.coordinator.server.processor;

import org.apache.commons.lang3.StringUtils;

import io.simforce.bytezard.common.entity.TaskRequest;
import io.simforce.bytezard.common.utils.JSONUtils;
import io.simforce.bytezard.coordinator.server.cache.JobExecuteManager;
import io.simforce.bytezard.remote.command.Command;
import io.simforce.bytezard.remote.command.CommandCode;
import io.simforce.bytezard.remote.command.JobReportInfoCommand;
import io.simforce.bytezard.remote.processor.NettyEventProcessor;
import io.simforce.bytezard.remote.utils.ChannelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;
import io.simforce.bytezard.remote.utils.JsonSerializer;

public class JobReportInfoProcessor implements NettyEventProcessor {

    private final Logger logger = LoggerFactory.getLogger(JobReportInfoProcessor.class);

    private final JobExecuteManager jobExecuteManager;

    public JobReportInfoProcessor(JobExecuteManager jobExecuteManager){
        this.jobExecuteManager = jobExecuteManager;

    }

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(
                CommandCode.JOB_REPORT_INFO == command.getCode(),
                String.format("invalid command type : %s", command.getCode()));
        JobReportInfoCommand jobReportInfoCommand = JsonSerializer.deserialize(new String(command.getBody()), JobReportInfoCommand.class);
        logger.info(JSONUtils.toJsonString(jobReportInfoCommand));

        TaskRequest taskRequest = jobExecuteManager.getExecutionJob(jobReportInfoCommand.getTaskId());
        if(taskRequest == null){
            taskRequest =  new TaskRequest();
        }

        taskRequest.setTaskId(jobReportInfoCommand.getTaskId());
        String workerAddress = ChannelUtils.toAddress(channel).getAddress();
        taskRequest.setExecuteHost(workerAddress);
        String applicationId = taskRequest.getApplicationId();
        if (StringUtils.isEmpty(applicationId)) {
            taskRequest.setApplicationId(jobReportInfoCommand.getApplicationIds());
        } else {
            if (StringUtils.isNotEmpty(jobReportInfoCommand.getApplicationIds()) &&
                    !(applicationId.equals(jobReportInfoCommand.getApplicationIds()))) {
                taskRequest.setApplicationId(jobReportInfoCommand.getApplicationIds());
            }
        }

        JobResponseContext jobResponseContext = new JobResponseContext(CommandCode.JOB_EXECUTE_ACK, taskRequest);
        jobExecuteManager.putResponse(jobResponseContext);

    }
}
