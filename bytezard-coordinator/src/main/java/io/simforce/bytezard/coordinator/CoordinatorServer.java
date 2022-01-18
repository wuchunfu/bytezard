package io.simforce.bytezard.coordinator;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.simforce.bytezard.common.elector.LeaderElectionAgent;
import io.simforce.bytezard.common.exception.ConfigurationException;
import io.simforce.bytezard.common.utils.Stopper;
import io.simforce.bytezard.common.utils.ThreadUtils;
import io.simforce.bytezard.common.zookeeper.ZooKeeperClient;
import io.simforce.bytezard.common.zookeeper.ZooKeeperConfig;
import io.simforce.bytezard.coordinator.api.ApiServer;
import io.simforce.bytezard.coordinator.server.cache.JobExecuteManager;
import io.simforce.bytezard.coordinator.config.CoordinatorConfiguration;
import io.simforce.bytezard.coordinator.server.listener.RemoveCacheChannelListener;
import io.simforce.bytezard.coordinator.server.log.LogService;
import io.simforce.bytezard.coordinator.server.processor.GetLogBytesProcessor;
import io.simforce.bytezard.coordinator.server.processor.JobAckProcessor;
import io.simforce.bytezard.coordinator.server.processor.JobExecuteResponseProcessor;
import io.simforce.bytezard.coordinator.server.processor.JobKillResponseProcessor;
import io.simforce.bytezard.coordinator.server.processor.JobReportInfoProcessor;
import io.simforce.bytezard.coordinator.server.processor.JobSubmitKillRequestProcessor;
import io.simforce.bytezard.coordinator.server.processor.JobSubmitRequestProcessor;
import io.simforce.bytezard.coordinator.server.processor.LogRollViewProcessor;
import io.simforce.bytezard.coordinator.server.processor.PingProcessor;
import io.simforce.bytezard.coordinator.server.processor.ViewWholeLogProcessor;
import io.simforce.bytezard.coordinator.server.recovery.MetaDataRecover;
import io.simforce.bytezard.coordinator.server.recovery.elector.CoordinatorLeaderElector;
import io.simforce.bytezard.coordinator.server.recovery.elector.ZooKeeperLeaderElectionAgent;
import io.simforce.bytezard.coordinator.server.recovery.factory.RecoveryModeFactory;
import io.simforce.bytezard.coordinator.server.recovery.factory.RecoveryModeFactoryManager;
import io.simforce.bytezard.coordinator.server.runner.JobScheduler;
import io.simforce.bytezard.remote.BytezardRemoteServer;
import io.simforce.bytezard.remote.command.CommandCode;
import io.simforce.bytezard.remote.config.NettyServerConfig;

public class CoordinatorServer {

    private static final Logger logger = LoggerFactory.getLogger(CoordinatorServer.class);

    private BytezardRemoteServer bytezardRemoteServer;

    private final CoordinatorConfiguration configuration;

    private MetaDataRecover metaDataRecover;

    private ApiServer apiServer;

    private JobExecuteManager jobExecuteManager;

    private CoordinatorServer(CoordinatorConfiguration configuration){
        this.configuration = configuration;
    }

    public static void main(String[] args){

        try{
            //读取配置文件，构造Configuration
            logger.info("starting coordinator server");
            if(args.length == 0){
                throw new ConfigurationException("configuration file path cannot be empty");
            }

            String configPath = args[0];
            if(StringUtils.isEmpty(configPath)){
                throw new ConfigurationException("configuration file path cannot be empty");
            }

            CoordinatorConfiguration configuration = CoordinatorConfiguration.getInstance();
            configuration.parse(configPath);

            CoordinatorServer coordinatorServer = new CoordinatorServer(configuration);
            coordinatorServer.initializeAndStart();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeAndStart() throws Exception {
        logger.info("coordinator server start");

        ZooKeeperClient.getInstance().buildClient(
                new ZooKeeperConfig(this.configuration.getString(
                        CoordinatorConfiguration.ZOOKEEPER_QUORUM,
                        CoordinatorConfiguration.ZOOKEEPER_QUORUM_DEFAULT)));

        RecoveryModeFactoryManager recoveryModeFactoryManager = new RecoveryModeFactoryManager();

        RecoveryModeFactory recoveryModeFactory =
                recoveryModeFactoryManager.getRecoveryModeFactory(
                        this.configuration.getString(
                                CoordinatorConfiguration.COORDINATOR_RECOVERY_MODE,
                                CoordinatorConfiguration.COORDINATOR_RECOVERY_MODE_DEFAULT));

        jobExecuteManager =
                new JobExecuteManager(this.configuration,recoveryModeFactory.createPersistenceEngine());

        metaDataRecover =
                new MetaDataRecover(this.configuration,recoveryModeFactory.createPersistenceEngine(),jobExecuteManager);

        startElectLeader();

        LogService logService = new LogService(jobExecuteManager,recoveryModeFactory.createPersistenceEngine());

        NettyServerConfig serverConfig = new NettyServerConfig();
        serverConfig.setListenPort(this.configuration.getInt(
                CoordinatorConfiguration.COORDINATOR_LISTEN_PORT,
                CoordinatorConfiguration.COORDINATOR_LISTEN_PORT_DEFAULT));
        this.bytezardRemoteServer = new BytezardRemoteServer(serverConfig);
        this.bytezardRemoteServer.registerProcessor(CommandCode.JOB_EXECUTE_RESPONSE,new JobExecuteResponseProcessor(jobExecuteManager));
        this.bytezardRemoteServer.registerProcessor(CommandCode.JOB_KILL_RESPONSE,new JobKillResponseProcessor(jobExecuteManager));
        this.bytezardRemoteServer.registerProcessor(CommandCode.JOB_EXECUTE_ACK,new JobAckProcessor(jobExecuteManager));
        this.bytezardRemoteServer.registerProcessor(CommandCode.PING,new PingProcessor());
        this.bytezardRemoteServer.registerProcessor(CommandCode.JOB_REPORT_INFO,new JobReportInfoProcessor(jobExecuteManager));
        this.bytezardRemoteServer.registerProcessor(CommandCode.JOB_SUBMIT_REQUEST,new JobSubmitRequestProcessor(jobExecuteManager));
        this.bytezardRemoteServer.registerProcessor(CommandCode.JOB_SUBMIT_KILL_REQUEST,new JobSubmitKillRequestProcessor(jobExecuteManager));
        this.bytezardRemoteServer.registerProcessor(CommandCode.ROLL_VIEW_LOG_REQUEST,new LogRollViewProcessor(logService));
        this.bytezardRemoteServer.registerProcessor(CommandCode.VIEW_WHOLE_LOG_REQUEST,new ViewWholeLogProcessor(logService));
        this.bytezardRemoteServer.registerProcessor(CommandCode.GET_LOG_BYTES_REQUEST,new GetLogBytesProcessor(logService));
        this.bytezardRemoteServer.addListener(new RemoveCacheChannelListener(jobExecuteManager));
        this.bytezardRemoteServer.start();

        logger.info("leader elect finish");

        //start job scheduler
        JobScheduler jobScheduler = new JobScheduler(jobExecuteManager);
        jobScheduler.start();

        //start api server
        apiServer = new ApiServer(configuration);
        apiServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                close("shutdownHook");
            }
        }));
    }

    private void startElectLeader() {
        CoordinatorLeaderElector coordinatorLeaderElector = new CoordinatorLeaderElector(configuration,metaDataRecover);
        LeaderElectionAgent leaderElectionAgent = new ZooKeeperLeaderElectionAgent();
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

            this.bytezardRemoteServer.close();

            this.metaDataRecover.close();

            this.apiServer.stop();

            this.jobExecuteManager.close();

        } catch (Exception e) {
            logger.error("coordinator server stop exception ", e);
            System.exit(-1);
        }
    }
}
