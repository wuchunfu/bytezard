package io.simforce.bytezard.coordinator.server.recovery;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.List;

import io.simforce.bytezard.common.config.CoreConfig;
import io.simforce.bytezard.common.entity.ExecutionJob;
import io.simforce.bytezard.common.utils.Stopper;
import io.simforce.bytezard.common.zookeeper.ZooKeeperClient;
import io.simforce.bytezard.coordinator.server.cache.JobExecuteManager;
import io.simforce.bytezard.coordinator.config.CoordinatorConfiguration;
import io.simforce.bytezard.remote.command.Command;
import io.simforce.bytezard.remote.command.JobExecuteAckCommand;
import io.simforce.bytezard.remote.command.JobExecuteResponseCommand;
import io.simforce.bytezard.remote.utils.FastJsonSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;

/**
 * @author zixi0825
 */
public class MetaDataRecover {

    private final Logger logger = LoggerFactory.getLogger(MetaDataRecover.class);

    private final CoordinatorConfiguration configuration;

    private final JobExecuteManager jobExecuteManager;

    private final ZooKeeperClient zooKeeperClient;

    private final PersistenceEngine persistenceEngine;

    private volatile boolean isStop = false;

    private RecoveryState recoveryState = RecoveryState.RECOVERING;

    public MetaDataRecover(CoordinatorConfiguration configuration,
                           PersistenceEngine persistenceEngine,
                           JobExecuteManager jobExecuteManager){
        this.persistenceEngine = persistenceEngine;
        this.configuration = configuration;
        this.jobExecuteManager = jobExecuteManager;
        this.zooKeeperClient = ZooKeeperClient.getInstance();
    }

    public void startRecoverMeta(){
        this.recoveryState = RecoveryState.RECOVERING;

        //处理缓存在zk中的response
        processCachedResponse();

        List<ExecutionJob> unStartedJob = persistenceEngine.getUnStartedJobs();
        List<ExecutionJob> unFinishedJob = persistenceEngine.getUnFinishedJobs();

//        logger.info(JSONUtils.toJSONString(unStartedJob));
//        logger.info(JSONUtils.toJSONString(unFinishedJob));

        //取出没有发送的任务填充到待发送队列
        jobExecuteManager.putUnStartedJobs(unStartedJob);
        jobExecuteManager.putUnFinishedJobs(unFinishedJob);

        //同时需要检查未完成任务的executor是否已经挂掉了，如果挂掉了需要

        this.recoveryState = RecoveryState.COMPLETED_RECOVERY;

        jobExecuteManager.start();
    }

    private void processCachedResponse() {

        InterProcessMutex mutex = null;
        try{
            mutex = zooKeeperClient.blockAcquireMutex(CoreConfig.JOB_COORDINATOR_RESPONSE_CACHE_LOCK_PATH);
            while (Stopper.isRunning() && !isStop) {
                List<String> cacheJobs = null;
                try {
                    cacheJobs = zooKeeperClient.getChildrenKeys(CoreConfig.JOB_RESPONSE_CACHE_PATH);
                    if (CollectionUtils.isNotEmpty(cacheJobs)) {
                        //key = jobInstanceId
                        for (String key:cacheJobs) {
                            String commandStr = zooKeeperClient.get(CoreConfig.JOB_RESPONSE_CACHE_PATH+"/"+key);
                            Command command = FastJsonSerializer.deserialize(commandStr.getBytes(),Command.class);
                            ExecutionJob executionJob = getExecutionJobByCommandCode(command);
                            persistenceEngine.update("", executionJob);
                            zooKeeperClient.remove(CoreConfig.JOB_RESPONSE_CACHE_PATH+"/"+key);
                        }
                    } else {
                        logger.info("there is no cache response,exit processor");
                        isStop = true;
                    }
                } catch (RuntimeException e) {
                    isStop = true;
                    logger.error("get cache error {0}",e);
                }
            }
        } catch (Exception e) {
            logger.error("acquire lock error,{0}",e);
        } finally {
            try {
                if (mutex != null && mutex.isAcquiredInThisProcess()) {
                    mutex.release();
                }
            } catch (Exception e) {
                logger.error("release lock error {0}",e);
            }
        }
    }

    public RecoveryState getRecoveryState() {
        return recoveryState;
    }

    private ExecutionJob getExecutionJobByCommandCode(Command command) {

        ExecutionJob executionJob = new ExecutionJob();

        switch (command.getCode()){
            case JOB_EXECUTE_ACK:
                JobExecuteAckCommand jobAckCommand = JSON.parseObject(new String(command.getBody()), JobExecuteAckCommand.class);
                executionJob.setJobInstanceId(jobAckCommand.getJobInstanceId());
                executionJob.setStartTime(jobAckCommand.getStartTime());
                executionJob.setStatus(jobAckCommand.getStatus());
                executionJob.setLogPath(jobAckCommand.getLogPath());
                executionJob.setExecutePath(jobAckCommand.getExecutePath());
                break;
            case JOB_EXECUTE_RESPONSE:
                JobExecuteResponseCommand jobExecuteResponseCommand = FastJsonSerializer.deserialize(command.getBody(), JobExecuteResponseCommand.class);
                executionJob.setJobInstanceId(jobExecuteResponseCommand.getJobInstanceId());
                executionJob.setEndTime(jobExecuteResponseCommand.getEndTime());
                executionJob.setStatus(jobExecuteResponseCommand.getStatus());
                executionJob.setApplicationIds(jobExecuteResponseCommand.getApplicationIds());
                executionJob.setProcessId(jobExecuteResponseCommand.getProcessId());
                break;
            case JOB_KILL_RESPONSE:
                break;
            default:
                break;
        }

        return executionJob;
    }

    public void close(){
        this.jobExecuteManager.close();
        isStop = true;
    }
}
