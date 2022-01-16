package io.simforce.bytezard.executor.processor;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import io.simforce.bytezard.common.config.CoreConfig;
import io.simforce.bytezard.common.zookeeper.ZooKeeperClient;
import io.simforce.bytezard.remote.command.Command;
import io.simforce.bytezard.remote.connection.Connection;
import io.simforce.bytezard.remote.connection.ServerConnectionManager;
import io.simforce.bytezard.remote.utils.FastJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class JobCallbackService {

    private final Logger logger = LoggerFactory.getLogger(JobCallbackService.class);

    private final ServerConnectionManager connectionManager;

    private final ZooKeeperClient zooKeeperClient;

    public JobCallbackService(){
        this.connectionManager = ServerConnectionManager.getInstance();
        this.zooKeeperClient = ZooKeeperClient.getInstance();
    }

    public void sendAck(long id,Command command){
        this.send(id,command);
    }

    public void sendResult(long id,Command command){
       this.send(id,command);
    }

    public void send(long id,Command command){
        Connection connection = connectionManager
                .getConnection();

        if (connection == null) {
            //将数据序列化以后写入zk的指定目录下
            InterProcessMutex mutex = null;
            try {
                mutex = zooKeeperClient.blockAcquireMutex(CoreConfig.JOB_COORDINATOR_RESPONSE_CACHE_LOCK_PATH);
                if (connectionManager.getConnection() == null) {
                    logger.info("master is not available,now cache the response in zk");
                    zooKeeperClient.persist(CoreConfig.JOB_RESPONSE_CACHE_PATH+"/"+id,FastJsonSerializer.serializeToString(command));
                } else {
                    connectionManager.getConnection().writeAndFlush(command);
                }
            } catch (Exception e) {
                logger.error("cache job error {0}",e);
            } finally {
                if(mutex != null && mutex.isAcquiredInThisProcess()){
                    try{
                        mutex.release();
                    }catch (Exception e) {
                        logger.error("release lock error {0}",e);
                    }
                }
            }
        } else {
            logger.info(JSON.toJSONString(connection));
            connection.writeAndFlush(command);
        }
    }
}
