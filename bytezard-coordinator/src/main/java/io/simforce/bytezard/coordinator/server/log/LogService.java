package io.simforce.bytezard.coordinator.server.log;

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.support.json.JSONUtils;

import io.simforce.bytezard.common.entity.ExecutionJob;
import io.simforce.bytezard.common.entity.LogResult;
import io.simforce.bytezard.coordinator.server.cache.JobExecuteManager;
import io.simforce.bytezard.coordinator.server.channel.ClientChannel;
import io.simforce.bytezard.coordinator.server.channel.ClientChannelManager;
import io.simforce.bytezard.coordinator.server.recovery.PersistenceEngine;
import io.simforce.bytezard.remote.command.Command;
import io.simforce.bytezard.remote.command.log.GetLogBytesRequestCommand;
import io.simforce.bytezard.remote.command.log.GetLogBytesResponseCommand;
import io.simforce.bytezard.remote.command.log.RollViewLogRequestCommand;
import io.simforce.bytezard.remote.command.log.RollViewLogResponseCommand;
import io.simforce.bytezard.remote.command.log.ViewLogRequestCommand;
import io.simforce.bytezard.remote.command.log.ViewLogResponseCommand;
import io.simforce.bytezard.remote.utils.FastJsonSerializer;

/**
 * @author zixi0825
 */
public class LogService {

    private final Logger logger = LoggerFactory.getLogger(LogService.class);

    private final JobExecuteManager jobExecuteManager;

    private final ClientChannelManager clientChannelManager;

    private final PersistenceEngine databasePersistenceEngine;

    public LogService(JobExecuteManager jobExecuteManager,PersistenceEngine databasePersistenceEngine){
        this.jobExecuteManager = jobExecuteManager;
        this.clientChannelManager = ClientChannelManager.getInstance();
        this.databasePersistenceEngine = databasePersistenceEngine;
    }

    public LogResult queryLog(long jobInstanceId, int offsetLine){
        return this.queryLog(jobInstanceId,offsetLine,10000);
    }

    public LogResult queryLog(long jobInstanceId,int offsetLine,int limit){

        ExecutionJob job = getExecutionJob(jobInstanceId);
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
            RollViewLogResponseCommand command = FastJsonSerializer.deserialize(result.getBody(), RollViewLogResponseCommand.class);
            return new LogResult(command.getMsg(),command.getOffsetLine());
        }

        return null;
    }

    public LogResult queryWholeLog(long jobInstanceId){

        ExecutionJob job = getExecutionJob(jobInstanceId);
        if (job == null) {
            return null;
        }

        ClientChannel clientChannel = getClientChannel(job);
        if (clientChannel == null) {
            return null;
        }

        ViewLogRequestCommand viewLogRequestCommand = new ViewLogRequestCommand();
        viewLogRequestCommand.setPath(job.getLogPath());
        logger.info("send command {}", JSONUtils.toJSONString(viewLogRequestCommand));
        Command result = clientChannel.sendSync(viewLogRequestCommand.convert2Command(),10000);
        if(result != null){
            ViewLogResponseCommand command = FastJsonSerializer.deserialize(result.getBody(),ViewLogResponseCommand.class);
            return new LogResult(command.getMsg(),0);
        }

        return null;
    }

    public byte[] getLogBytes(long jobInstanceId){

        ExecutionJob job = getExecutionJob(jobInstanceId);
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
            GetLogBytesResponseCommand command = FastJsonSerializer.deserialize(result.getBody(),GetLogBytesResponseCommand.class);
            return command.getMsg();
        }

        return null;
    }

    private ClientChannel getClientChannel(ExecutionJob job) {
        ClientChannel clientChannel = clientChannelManager.getExecutorByIp(job.getExecuteHost().split(":")[0]);
        if(clientChannel == null || clientChannel.getChannel() == null){
            logger.info("executor is not exist,can not get the log");
            return null;
        }
        return clientChannel;
    }

    private ExecutionJob getExecutionJob(long jobInstanceId) {
        ExecutionJob job = jobExecuteManager.getExecutionJob(jobInstanceId);

        if(job == null || StringUtils.isEmpty(job.getLogPath())){
            job = databasePersistenceEngine.getById(jobInstanceId);

            if(job == null || StringUtils.isEmpty(job.getLogPath())){
                logger.info("job {} is not exist",jobInstanceId);
                return null;
            }
        }
        return job;
    }
}