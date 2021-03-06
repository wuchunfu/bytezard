package io.datavines.executor.cache;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.List;

import io.datavines.common.config.CoreConfig;
import io.datavines.common.utils.Stopper;
import io.datavines.common.zookeeper.ZooKeeperClient;
import io.datavines.remote.command.Command;
import io.datavines.remote.connection.ServerConnectionManager;
import io.datavines.remote.utils.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zixi0825
 */
public class JobResponseCacheProcessor extends Thread {

    private final Logger logger = LoggerFactory.getLogger(JobResponseCacheProcessor.class);

    private final ZooKeeperClient zooKeeperClient;

    private final ServerConnectionManager serverConnectionManager;

    private volatile boolean isStop = false;

    public JobResponseCacheProcessor(){
        this.zooKeeperClient = ZooKeeperClient.getInstance();
        this.serverConnectionManager = ServerConnectionManager.getInstance();
    }

    @Override
    public void run() {
        InterProcessMutex mutex = null;
        try{
            mutex = zooKeeperClient.blockAcquireMutex(CoreConfig.JOB_COORDINATOR_RESPONSE_CACHE_LOCK_PATH);
            while (Stopper.isRunning() && !isStop) {
                List<String> cacheJobs = null;
                try {
                    cacheJobs = zooKeeperClient.getChildrenKeys(CoreConfig.JOB_RESPONSE_CACHE_PATH);
                } catch (RuntimeException e) {
                    isStop = true;
                    logger.error("get cache error {0}",e);
                }

                if(CollectionUtils.isNotEmpty(cacheJobs)) {
                    //key = taskId_CommandType
                    for (String key:cacheJobs) {

                        String commandStr = zooKeeperClient.get(CoreConfig.JOB_RESPONSE_CACHE_PATH+"/"+key);
                        Command command = JsonSerializer.deserialize(commandStr.getBytes(),Command.class);

                        if (command != null) {
                            if (serverConnectionManager.getConnection() == null
                                    || !serverConnectionManager.getConnection().isActive()) {
                                logger.info("coordinator is not available,exit processor");
                                isStop = true;
                            } else {
                                serverConnectionManager.getConnection().writeAndFlush(command);
                                zooKeeperClient.remove(CoreConfig.JOB_RESPONSE_CACHE_PATH+"/"+key);
                            }
                        }
                    }
                } else {
                    logger.info("there is no cache response,exit processor");
                    isStop = true;
                }
            }
        } catch (Exception e) {
            logger.error("acquire lock error,{0}",e);
        } finally {
            try{
                if(mutex != null && mutex.isAcquiredInThisProcess()){
                    mutex.release();
                }
            }catch (Exception e){
                logger.error("release lock error {0}",e);
            }
        }
    }
}
