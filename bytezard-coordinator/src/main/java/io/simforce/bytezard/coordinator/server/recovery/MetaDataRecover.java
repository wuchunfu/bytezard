package io.simforce.bytezard.coordinator.server.recovery;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.List;

import io.simforce.bytezard.common.config.CoreConfig;
import io.simforce.bytezard.common.entity.TaskRequest;
import io.simforce.bytezard.common.utils.Stopper;
import io.simforce.bytezard.common.zookeeper.ZooKeeperClient;
import io.simforce.bytezard.coordinator.server.cache.JobExecuteManager;
import io.simforce.bytezard.coordinator.config.CoordinatorConfiguration;
import io.simforce.bytezard.remote.command.Command;
import io.simforce.bytezard.remote.command.JobExecuteAckCommand;
import io.simforce.bytezard.remote.command.JobExecuteResponseCommand;
import io.simforce.bytezard.remote.utils.JsonSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        List<TaskRequest> unStartedJob = persistenceEngine.getUnStartedJobs();
        List<TaskRequest> unFinishedJob = persistenceEngine.getUnFinishedJobs();

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
                        //key = taskId
                        for (String key:cacheJobs) {
                            String commandStr = zooKeeperClient.get(CoreConfig.JOB_RESPONSE_CACHE_PATH+"/"+key);
                            Command command = JsonSerializer.deserialize(commandStr.getBytes(),Command.class);
                            TaskRequest taskRequest = getExecutionJobByCommandCode(command);
                            persistenceEngine.update("", taskRequest);
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

    private TaskRequest getExecutionJobByCommandCode(Command command) {

        TaskRequest taskRequest = new TaskRequest();

        switch (command.getCode()){
            case JOB_EXECUTE_ACK:
                JobExecuteAckCommand jobAckCommand = JsonSerializer.deserialize(new String(command.getBody()), JobExecuteAckCommand.class);
                taskRequest.setTaskId(jobAckCommand.getTaskId());
                taskRequest.setStartTime(jobAckCommand.getStartTime());
                taskRequest.setStatus(jobAckCommand.getStatus());
                taskRequest.setLogPath(jobAckCommand.getLogPath());
                taskRequest.setExecuteFilePath(jobAckCommand.getExecutePath());
                break;
            case JOB_EXECUTE_RESPONSE:
                JobExecuteResponseCommand jobExecuteResponseCommand = JsonSerializer.deserialize(command.getBody(), JobExecuteResponseCommand.class);
                taskRequest.setTaskId(jobExecuteResponseCommand.getTaskId());
                taskRequest.setEndTime(jobExecuteResponseCommand.getEndTime());
                taskRequest.setStatus(jobExecuteResponseCommand.getStatus());
                taskRequest.setApplicationId(jobExecuteResponseCommand.getApplicationIds());
                taskRequest.setProcessId(jobExecuteResponseCommand.getProcessId());
                break;
            case JOB_KILL_RESPONSE:
                break;
            default:
                break;
        }

        return taskRequest;
    }

    public void close(){
        this.jobExecuteManager.close();
        isStop = true;
    }
}
