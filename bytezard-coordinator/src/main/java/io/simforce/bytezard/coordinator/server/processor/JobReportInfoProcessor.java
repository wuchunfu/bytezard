package io.simforce.bytezard.coordinator.server.processor;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

import io.simforce.bytezard.common.entity.ExecutionJob;
import io.simforce.bytezard.coordinator.server.cache.JobExecuteManager;
import io.simforce.bytezard.remote.command.Command;
import io.simforce.bytezard.remote.command.CommandCode;
import io.simforce.bytezard.remote.command.JobReportInfoCommand;
import io.simforce.bytezard.remote.processor.NettyEventProcessor;
import io.simforce.bytezard.remote.utils.ChannelUtils;
import io.simforce.bytezard.remote.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

/**
 * @author zixi0825
 */
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
        JobReportInfoCommand jobReportInfoCommand = JSON.parseObject(new String(command.getBody()), JobReportInfoCommand.class);
        logger.info(JSONUtils.toJSONString(jobReportInfoCommand));

        ExecutionJob executionJob = jobExecuteManager.getExecutionJob(jobReportInfoCommand.getJobInstanceId());
        if(executionJob == null){
            executionJob =  new ExecutionJob();
        }

        executionJob.setJobInstanceId(jobReportInfoCommand.getJobInstanceId());
        String workerAddress = ChannelUtils.toAddress(channel).getAddress();
        executionJob.setExecuteHost(workerAddress);
        String applicationIds = executionJob.getApplicationId();
        if(StringUtils.isEmpty(applicationIds)){
            executionJob.setApplicationIds(jobReportInfoCommand.getApplicationIds());
        }else{
            if(StringUtils.isNotEmpty(jobReportInfoCommand.getApplicationIds())){
                List<String> appIdList = Arrays.asList(applicationIds.split(Constants.COMMA));
                String[] appIds = jobReportInfoCommand.getApplicationIds().split(Constants.COMMA);
                for(String appId:appIds){
                    if(!appIdList.contains(appId)){
                        appIdList.add(appId);
                    }
                }

                executionJob.setApplicationIds(String.join(Constants.COMMA, appIdList));
            }
        }

        JobResponseContext jobResponseContext = new JobResponseContext(CommandCode.JOB_EXECUTE_ACK, executionJob);
        jobExecuteManager.putResponse(jobResponseContext);

    }
}
