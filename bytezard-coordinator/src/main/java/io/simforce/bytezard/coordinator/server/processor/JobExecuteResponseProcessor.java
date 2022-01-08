package io.simforce.bytezard.coordinator.server.processor;

import io.simforce.bytezard.common.entity.ExecutionJob;
import io.simforce.bytezard.coordinator.server.cache.JobExecuteManager;
import io.simforce.bytezard.remote.command.Command;
import io.simforce.bytezard.remote.command.CommandCode;
import io.simforce.bytezard.remote.command.JobExecuteResponseCommand;
import io.simforce.bytezard.remote.processor.NettyEventProcessor;
import io.simforce.bytezard.remote.utils.ChannelUtils;
import io.simforce.bytezard.remote.utils.FastJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.support.json.JSONUtils;
import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

/**
 * @author zixi0825
 */
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
        JobExecuteResponseCommand jobExecuteResponseCommand = FastJsonSerializer.deserialize(command.getBody(), JobExecuteResponseCommand.class);
        logger.info("jobExecuteResponseCommand : {}", JSONUtils.toJSONString(jobExecuteResponseCommand));

        ExecutionJob executionJob = jobExecuteManager.getExecutionJob(jobExecuteResponseCommand.getJobInstanceId());
        if(executionJob == null){
            executionJob =  new ExecutionJob();
        }
        executionJob.setJobInstanceId(jobExecuteResponseCommand.getJobInstanceId());
        executionJob.setEndTime(jobExecuteResponseCommand.getEndTime());
        executionJob.setStatus(jobExecuteResponseCommand.getStatus());
        String workerAddress = ChannelUtils.toAddress(channel).getAddress();
        executionJob.setExecuteHost(workerAddress);
        executionJob.setApplicationIds(jobExecuteResponseCommand.getApplicationIds());
        executionJob.setProcessId(jobExecuteResponseCommand.getProcessId());

        JobResponseContext jobResponseContext = new JobResponseContext(CommandCode.JOB_EXECUTE_RESPONSE, executionJob);

        jobExecuteManager.putResponse(jobResponseContext);
    }
}
