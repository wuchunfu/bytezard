package io.simforce.bytezard.coordinator.server.processor;

import io.simforce.bytezard.common.entity.ExecutionJob;
import io.simforce.bytezard.coordinator.server.cache.JobExecuteManager;
import io.simforce.bytezard.remote.command.Command;
import io.simforce.bytezard.remote.command.CommandCode;
import io.simforce.bytezard.remote.command.JobExecuteAckCommand;
import io.simforce.bytezard.remote.processor.NettyEventProcessor;
import io.simforce.bytezard.remote.utils.ChannelUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;

import io.netty.channel.Channel;


/**
 * @author zixi0825
 */
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
        JobExecuteAckCommand jobAckCommand = JSON.parseObject(new String(command.getBody()), JobExecuteAckCommand.class);
        logger.info(JSONUtils.toJSONString(jobAckCommand));

        ExecutionJob executionJob = jobExecuteManager.getExecutionJob(jobAckCommand.getJobInstanceId());
        if(executionJob == null){
            executionJob =  new ExecutionJob();
        }

        executionJob.setJobInstanceId(jobAckCommand.getJobInstanceId());
        executionJob.setStartTime(jobAckCommand.getStartTime());
        executionJob.setStatus(jobAckCommand.getStatus());
        String workerAddress = ChannelUtils.toAddress(channel).getAddress();
        executionJob.setExecuteHost(workerAddress);
        executionJob.setLogPath(jobAckCommand.getLogPath());
        executionJob.setExecutePath(jobAckCommand.getExecutePath());

        JobResponseContext jobResponseContext = new JobResponseContext(CommandCode.JOB_EXECUTE_ACK, executionJob);

        jobExecuteManager.putResponse(jobResponseContext);

    }
}
