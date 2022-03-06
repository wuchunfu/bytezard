package io.datavines.remote.connection;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.datavines.common.entity.ActiveNodeInfo;
import io.datavines.remote.DataVinesRemoteClient;
import io.datavines.remote.command.PingCommand;
import io.datavines.remote.command.RequestClientType;
import io.datavines.remote.utils.JsonSerializer;
import io.datavines.remote.utils.Host;
import io.datavines.remote.utils.NamedThreadFactory;
import io.datavines.common.registry.IRegistryCenter;
import io.datavines.common.registry.ZooKeeperRegistryCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

/**
 * @author zixi0825
 */
public abstract class ServerReConnector {

    private final Logger logger = LoggerFactory.getLogger(ServerReConnector.class);

    protected IRegistryCenter registryCenter;

    private final ScheduledExecutorService scheduledExecutorService;

    private final ServerConnectionManager serverConnectionManager;

    private volatile boolean isRunning = true;

    public ServerReConnector(){
        this.registryCenter = new ZooKeeperRegistryCenter();
        this.scheduledExecutorService =
                Executors.newScheduledThreadPool(1,new NamedThreadFactory("executor-server-scheduler"));
        this.serverConnectionManager = ServerConnectionManager.getInstance();
        isRunning = true;
    }

    public void doConnect(final DataVinesRemoteClient datavinesRemoteClient,
                          final Operator operator,
                          final RequestClientType clientType) {
        if (isRunning) {
            String masterNode = getActiveNode();
            if(null == masterNode || "".equals(masterNode)){
                this.scheduledExecutorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        doConnect(datavinesRemoteClient,operator,clientType);
                    }
                },10,TimeUnit.SECONDS);
            } else {
                ActiveNodeInfo masterInfo = JsonSerializer.deserialize(masterNode,ActiveNodeInfo.class);
                if(masterInfo != null) {
                    Host host = new Host(masterInfo.getIp(), masterInfo.getRpcPort());
                    Channel channel = datavinesRemoteClient.doConnect(host,true);
                    if(channel != null) {

                        Connection connection = new Connection(channel);
                        serverConnectionManager.setConnection(connection);

                        logger.info("connect sever success ");
                        if(operator != null){
                            operator.execute();
                        }

                        PingCommand pingCommand = new PingCommand();
                        pingCommand.setClientType(clientType);
                        channel.writeAndFlush(pingCommand.convert2Command());
                        //启动一个线程去获取zk上的数据，然后将数据发送到master，直到所有缓存的数据都发送完了就停止了
                    } else {
                        this.scheduledExecutorService.schedule(new Runnable() {
                            @Override
                            public void run() {
                                doConnect(datavinesRemoteClient,operator,clientType);
                            }
                        },2,TimeUnit.MILLISECONDS);
                    }
                }
            }
        }
    }

    public void close() {
        this.isRunning = false;
        this.scheduledExecutorService.shutdown();
    }

    public interface Operator {
        /**
         * do something
         */
        void execute();
    }

    public abstract String getActiveNode();

}
