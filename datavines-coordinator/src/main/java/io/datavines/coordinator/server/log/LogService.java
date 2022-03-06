package io.datavines.coordinator.server.log;

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datavines.common.entity.TaskRequest;
import io.datavines.common.entity.LogResult;
import io.datavines.common.utils.JSONUtils;
import io.datavines.coordinator.server.cache.JobExecuteManager;
import io.datavines.coordinator.server.channel.ClientChannel;
import io.datavines.coordinator.server.channel.ClientChannelManager;
import io.datavines.remote.command.Command;
import io.datavines.remote.command.log.GetLogBytesRequestCommand;
import io.datavines.remote.command.log.GetLogBytesResponseCommand;
import io.datavines.remote.command.log.RollViewLogRequestCommand;
import io.datavines.remote.command.log.RollViewLogResponseCommand;
import io.datavines.remote.command.log.ViewLogRequestCommand;
import io.datavines.remote.command.log.ViewLogResponseCommand;
import io.datavines.remote.utils.JsonSerializer;

public class LogService {

    private final Logger logger = LoggerFactory.getLogger(LogService.class);

    private final JobExecuteManager jobExecuteManager;

    private final ClientChannelManager clientChannelManager;

    public LogService(JobExecuteManager jobExecuteManager) {
        this.jobExecuteManager = jobExecuteManager;
        this.clientChannelManager = ClientChannelManager.getInstance();
    }

    public LogResult queryLog(long taskId, int offsetLine){
        return this.queryLog(taskId,offsetLine,10000);
    }

    public LogResult queryLog(long taskId,int offsetLine,int limit){

        TaskRequest job = getExecutionJob(taskId);
        if (job == null) {
            return null;
        }

        ClientChannel clientChannel = getClientChannel(job);
        if (clientChannel == null) {
            return null;
        }

        RollViewLogRequestCommand rollViewLogRequestCommand = new RollViewLogRequestCommand();
        rollViewLogRequestCommand.setPath(job.getLogPath());
        rollViewLogRequestCommand.setSkipLineNum(offsetLine);
        rollViewLogRequestCommand.setLimit(limit);

        Command result = clientChannel.sendSync(rollViewLogRequestCommand.convert2Command(),10000);
        if(result != null){
            RollViewLogResponseCommand command = JsonSerializer.deserialize(result.getBody(), RollViewLogResponseCommand.class);
            return new LogResult(command.getMsg(),command.getOffsetLine());
        }

        return null;
    }

    public LogResult queryWholeLog(long taskId){

        TaskRequest job = getExecutionJob(taskId);
        if (job == null) {
            return null;
        }

        ClientChannel clientChannel = getClientChannel(job);
        if (clientChannel == null) {
            return null;
        }

        ViewLogRequestCommand viewLogRequestCommand = new ViewLogRequestCommand();
        viewLogRequestCommand.setPath(job.getLogPath());
        logger.info("send command {}", JSONUtils.toJsonString(viewLogRequestCommand));
        Command result = clientChannel.sendSync(viewLogRequestCommand.convert2Command(),10000);
        if(result != null){
            ViewLogResponseCommand command = JsonSerializer.deserialize(result.getBody(),ViewLogResponseCommand.class);
            return new LogResult(command.getMsg(),0);
        }

        return null;
    }

    public byte[] getLogBytes(long taskId){

        TaskRequest job = getExecutionJob(taskId);
        if (job == null) {
            return null;
        }

        ClientChannel clientChannel = getClientChannel(job);
        if (clientChannel == null) {
            return null;
        }

        GetLogBytesRequestCommand getLogBytesRequestCommand = new GetLogBytesRequestCommand();
        getLogBytesRequestCommand.setPath(job.getLogPath());

        Command result = clientChannel.sendSync(getLogBytesRequestCommand.convert2Command(),10000);
        if(result != null){
            GetLogBytesResponseCommand command = JsonSerializer.deserialize(result.getBody(),GetLogBytesResponseCommand.class);
            return command.getMsg();
        }

        return null;
    }

    private ClientChannel getClientChannel(TaskRequest job) {
        ClientChannel clientChannel = clientChannelManager.getExecutorByIp(job.getExecuteHost().split(":")[0]);
        if(clientChannel == null || clientChannel.getChannel() == null){
            logger.info("executor is not exist,can not get the log");
            return null;
        }
        return clientChannel;
    }

    private TaskRequest getExecutionJob(long taskId) {
        TaskRequest job = jobExecuteManager.getExecutionJob(taskId);
        if(job == null || StringUtils.isEmpty(job.getLogPath())){
            logger.info("job {} is not exist",taskId);
            return null;
        }

        return job;
    }
}
