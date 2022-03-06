package io.datavines.coordinator;

import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.datavines.coordinator.server.recovery.elector.CoordinatorLeaderElector;
import io.datavines.coordinator.server.recovery.elector.ZooKeeperLeaderElectionAgent;
import io.datavines.common.elector.LeaderElectionAgent;
import io.datavines.common.utils.Stopper;
import io.datavines.common.utils.ThreadUtils;
import io.datavines.common.zookeeper.ZooKeeperClient;
import io.datavines.common.zookeeper.ZooKeeperConfig;
import io.datavines.coordinator.server.cache.JobExecuteManager;
import io.datavines.coordinator.config.CoordinatorConfiguration;
import io.datavines.coordinator.server.listener.RemoveCacheChannelListener;
import io.datavines.coordinator.server.log.LogService;
import io.datavines.coordinator.server.processor.GetLogBytesProcessor;
import io.datavines.coordinator.server.processor.JobAckProcessor;
import io.datavines.coordinator.server.processor.JobExecuteResponseProcessor;
import io.datavines.coordinator.server.processor.JobKillResponseProcessor;
import io.datavines.coordinator.server.processor.JobReportInfoProcessor;
import io.datavines.coordinator.server.processor.JobSubmitKillRequestProcessor;
import io.datavines.coordinator.server.processor.JobSubmitRequestProcessor;
import io.datavines.coordinator.server.processor.LogRollViewProcessor;
import io.datavines.coordinator.server.processor.PingProcessor;
import io.datavines.coordinator.server.processor.ViewWholeLogProcessor;
import io.datavines.coordinator.server.recovery.MetaDataRecover;
import io.datavines.coordinator.server.runner.JobScheduler;
import io.datavines.coordinator.utils.SpringApplicationContext;
import io.datavines.remote.DataVinesRemoteServer;
import io.datavines.remote.command.CommandCode;
import io.datavines.remote.config.NettyServerConfig;

@SpringBootApplication
public class CoordinatorServer {

    private static final Logger logger = LoggerFactory.getLogger(CoordinatorServer.class);

    @Autowired
    private SpringApplicationContext springApplicationContext;

    @Autowired
    private CoordinatorConfiguration configuration;

    private DataVinesRemoteServer datavinesRemoteServer;

    private MetaDataRecover metaDataRecover;

    private JobExecuteManager jobExecuteManager;

    public static void main(String[] args) {
        Thread.currentThread().setName(CoordinatorConstants.THREAD_NAME_COORDINATOR_SERVER);
        SpringApplication.run(CoordinatorServer.class);
    }

    @PostConstruct
    private void initializeAndStart() throws Exception {
        logger.info("coordinator server start");

        ZooKeeperClient.getInstance().buildClient(new ZooKeeperConfig(this.configuration.getZookeeperQuorum()));

        jobExecuteManager = new JobExecuteManager(this.configuration);

        metaDataRecover = new MetaDataRecover(jobExecuteManager);

        startElectLeader(this.configuration);

        LogService logService = new LogService(jobExecuteManager);

        NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(this.configuration.getListenPort());
        this.datavinesRemoteServer = new DataVinesRemoteServer(serverConfig);
        this.datavinesRemoteServer.registerProcessor(CommandCode.JOB_EXECUTE_RESPONSE,new JobExecuteResponseProcessor(jobExecuteManager));
        this.datavinesRemoteServer.registerProcessor(CommandCode.JOB_KILL_RESPONSE,new JobKillResponseProcessor(jobExecuteManager));
        this.datavinesRemoteServer.registerProcessor(CommandCode.JOB_EXECUTE_ACK,new JobAckProcessor(jobExecuteManager));
        this.datavinesRemoteServer.registerProcessor(CommandCode.PING,new PingProcessor());
        this.datavinesRemoteServer.registerProcessor(CommandCode.JOB_REPORT_INFO,new JobReportInfoProcessor(jobExecuteManager));
        this.datavinesRemoteServer.registerProcessor(CommandCode.JOB_SUBMIT_REQUEST,new JobSubmitRequestProcessor(jobExecuteManager));
        this.datavinesRemoteServer.registerProcessor(CommandCode.JOB_SUBMIT_KILL_REQUEST,new JobSubmitKillRequestProcessor(jobExecuteManager));
        this.datavinesRemoteServer.registerProcessor(CommandCode.ROLL_VIEW_LOG_REQUEST,new LogRollViewProcessor(logService));
        this.datavinesRemoteServer.registerProcessor(CommandCode.VIEW_WHOLE_LOG_REQUEST,new ViewWholeLogProcessor(logService));
        this.datavinesRemoteServer.registerProcessor(CommandCode.GET_LOG_BYTES_REQUEST,new GetLogBytesProcessor(logService));
        this.datavinesRemoteServer.addListener(new RemoveCacheChannelListener(jobExecuteManager));
        this.datavinesRemoteServer.start();

        logger.info("leader elect finish");

        //start job scheduler
        JobScheduler jobScheduler = new JobScheduler(jobExecuteManager);
        jobScheduler.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                close("shutdownHook");
            }
        }));
    }

    private void startElectLeader(CoordinatorConfiguration configuration) {
        CoordinatorLeaderElector coordinatorLeaderElector = new CoordinatorLeaderElector(configuration,metaDataRecover);
        LeaderElectionAgent leaderElectionAgent = new ZooKeeperLeaderElectionAgent(configuration);
        leaderElectionAgent.setLeaderElector(coordinatorLeaderElector);
        leaderElectionAgent.start();
        coordinatorLeaderElector.await();
    }

    /**
     * gracefully close
     * @param cause close cause
     */
    private void close(String cause) {

        try {
            //execute only once
            if(Stopper.isStopped()){
                return;
            }

            logger.info("coordinator server is stopping ..., cause : {}", cause);

            // set stop signal is true
            Stopper.stop();

            ThreadUtils.sleep(2000);

            this.datavinesRemoteServer.close();

            this.metaDataRecover.close();

            this.jobExecuteManager.close();

        } catch (Exception e) {
            logger.error("coordinator server stop exception ", e);
            System.exit(-1);
        }
    }
}
