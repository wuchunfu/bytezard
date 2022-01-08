package io.simforce.bytezard.coordinator.server.processor;

import io.simforce.bytezard.common.entity.ExecutionJob;
import io.simforce.bytezard.coordinator.server.cache.JobExecuteManager;
import io.simforce.bytezard.remote.command.Command;
import io.simforce.bytezard.remote.command.CommandCode;
import io.simforce.bytezard.remote.command.JobSubmitAckCommand;
import io.simforce.bytezard.remote.command.JobSubmitRequestCommand;
import io.simforce.bytezard.remote.processor.NettyEventProcessor;
import io.simforce.bytezard.remote.utils.ChannelUtils;
import io.simforce.bytezard.remote.utils.FastJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;

import io.netty.channel.Channel;


/**
 * @author zixi0825
 */
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
        JobSubmitRequestCommand jobSubmitRequestCommand = JSON.parseObject(new String(command.getBody()), JobSubmitRequestCommand.class);

        JobSubmitAckCommand jobSubmitAckCommand = new JobSubmitAckCommand();

        if(jobSubmitRequestCommand == null){
            jobSubmitAckCommand.setCode(0);
            jobSubmitAckCommand.setMsg("command is null");
            channel.writeAndFlush(jobSubmitAckCommand.convert2Command(command.getOpaque()));
            return;
        }

        ExecutionJob executionJob = FastJsonSerializer.deserialize(jobSubmitRequestCommand.getJobExecutionContext(),ExecutionJob.class);

        if(executionJob == null){
            jobSubmitAckCommand.setCode(0);
            jobSubmitAckCommand.setMsg("execution job is null");
            channel.writeAndFlush(jobSubmitAckCommand.convert2Command(command.getOpaque()));
            return;
        }

        long id = jobExecuteManager.getPersistenceEngine().persist("",executionJob);
        executionJob.setJobInstanceId(id);
        jobExecuteManager.addExecuteCommand(executionJob);
        jobExecuteManager.putJobInstanceId2ClientIpMap(
                executionJob.getJobInstanceId(),ChannelUtils.toAddress(channel).getIp());

        jobSubmitAckCommand.setJobInstanceId(id);
        jobSubmitAckCommand.setCode(1);
        jobSubmitAckCommand.setMsg("submit job success");
        channel.writeAndFlush(jobSubmitAckCommand.convert2Command(command.getOpaque()));
    }
}
