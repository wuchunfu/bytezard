package io.simforce.bytezard.executor;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.simforce.bytezard.common.exception.ConfigurationException;
import io.simforce.bytezard.common.utils.Stopper;
import io.simforce.bytezard.common.utils.ThreadUtils;
import io.simforce.bytezard.common.zookeeper.ZooKeeperClient;
import io.simforce.bytezard.common.zookeeper.ZooKeeperConfig;
import io.simforce.bytezard.executor.cache.JobResponseCacheProcessor;
import io.simforce.bytezard.executor.config.ExecutorConfiguration;
import io.simforce.bytezard.executor.listener.RemoveMasterChannelListener;
import io.simforce.bytezard.executor.processor.JobCallbackService;
import io.simforce.bytezard.executor.processor.JobExecuteProcessor;
import io.simforce.bytezard.executor.processor.JobKillProcessor;
import io.simforce.bytezard.executor.processor.CoordinatorServerReConnector;
import io.simforce.bytezard.executor.processor.PongProcessor;
import io.simforce.bytezard.executor.processor.log.GetLogBytesProcessor;
import io.simforce.bytezard.executor.processor.log.LogRollViewProcessor;
import io.simforce.bytezard.executor.processor.log.ViewWholeLogProcessor;
import io.simforce.bytezard.remote.BytezardRemoteClient;
import io.simforce.bytezard.remote.command.CommandCode;
import io.simforce.bytezard.remote.command.RequestClientType;
import io.simforce.bytezard.remote.config.NettyClientConfig;
import io.simforce.bytezard.remote.connection.ServerReConnector;

public class ExecutorServer {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorServer.class);

    private BytezardRemoteClient bytezardRemoteClient;

    private CoordinatorServerReConnector serverReConnector;

    private final ExecutorConfiguration configuration;

    private ExecutorServer(ExecutorConfiguration configuration){
        this.configuration = configuration;
    }

    public static void main(String[] args){

        try{
            //读取配置文件，构造Configuration
            logger.info("starting master server");
            if(args.length == 0){
                throw new ConfigurationException("configuration file path cannot be empty");
            }

            String configPath = args[0];
            if(StringUtils.isEmpty(configPath)){
                throw new ConfigurationException("configuration file path cannot be empty");
            }

            ExecutorConfiguration configuration = ExecutorConfiguration.getInstance();
            configuration.parse(configPath);

            ExecutorServer masterServer = new ExecutorServer(configuration);
            masterServer.initializeAndStart();

        } catch(ConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeAndStart() throws IOException,InterruptedException,Exception{
        logger.info("executor netty server start");

        ZooKeeperClient.getInstance().buildClient(
                new ZooKeeperConfig(this.configuration.getString(
                        ExecutorConfiguration.ZOOKEEPER_QUORUM,
                        ExecutorConfiguration.ZOOKEEPER_QUORUM_DEFAULT)));

        JobCallbackService jobCallbackService = new JobCallbackService();

        this.bytezardRemoteClient = new BytezardRemoteClient(new NettyClientConfig());
        this.bytezardRemoteClient.registerProcessor(CommandCode.PONG,new PongProcessor());
        this.bytezardRemoteClient.registerProcessor(CommandCode.JOB_EXECUTE_REQUEST,new JobExecuteProcessor(this.configuration,jobCallbackService));
        this.bytezardRemoteClient.registerProcessor(CommandCode.JOB_KILL_REQUEST,new JobKillProcessor());
        this.bytezardRemoteClient.registerProcessor(CommandCode.ROLL_VIEW_LOG_REQUEST,new LogRollViewProcessor());
        this.bytezardRemoteClient.registerProcessor(CommandCode.VIEW_WHOLE_LOG_REQUEST,new ViewWholeLogProcessor());
        this.bytezardRemoteClient.registerProcessor(CommandCode.GET_LOG_BYTES_REQUEST,new GetLogBytesProcessor());

        serverReConnector = new CoordinatorServerReConnector();
        serverReConnector.doConnect(this.bytezardRemoteClient, new ServerReConnector.Operator() {
            @Override
            public void execute() {

                ThreadUtils.sleep(2000);
                JobResponseCacheProcessor jobResponseCacheProcessor = new JobResponseCacheProcessor();
                jobResponseCacheProcessor.start();
            }
        }, RequestClientType.EXECUTOR);

        this.bytezardRemoteClient.addListener(new RemoveMasterChannelListener(serverReConnector));

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                close("shutdown executor");
            }
        });
    }

    private void close(String cause) {

        try {
            //execute only once
            if(Stopper.isStopped()){
                return;
            }

            logger.info("worker server is stopping ..., cause : {}", cause);

            // set stop signal is true
            Stopper.stop();

            ThreadUtils.sleep(3000);

            this.bytezardRemoteClient.close();

            if(serverReConnector != null){
                serverReConnector.close();
            }

        } catch (Exception e) {
            logger.error("worker server stop exception ", e);
            System.exit(-1);
        }
    }

}
