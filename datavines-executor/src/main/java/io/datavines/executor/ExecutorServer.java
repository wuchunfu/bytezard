package io.datavines.executor;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datavines.common.exception.ConfigurationException;
import io.datavines.common.utils.Stopper;
import io.datavines.common.utils.ThreadUtils;
import io.datavines.common.zookeeper.ZooKeeperClient;
import io.datavines.common.zookeeper.ZooKeeperConfig;
import io.datavines.executor.cache.JobResponseCacheProcessor;
import io.datavines.executor.config.ExecutorConfiguration;
import io.datavines.executor.listener.RemoveMasterChannelListener;
import io.datavines.executor.processor.JobCallbackService;
import io.datavines.executor.processor.JobExecuteProcessor;
import io.datavines.executor.processor.JobKillProcessor;
import io.datavines.executor.processor.CoordinatorServerReConnector;
import io.datavines.executor.processor.PongProcessor;
import io.datavines.executor.processor.log.GetLogBytesProcessor;
import io.datavines.executor.processor.log.LogRollViewProcessor;
import io.datavines.executor.processor.log.ViewWholeLogProcessor;
import io.datavines.remote.DataVinesRemoteClient;
import io.datavines.remote.command.CommandCode;
import io.datavines.remote.command.RequestClientType;
import io.datavines.remote.config.NettyClientConfig;
import io.datavines.remote.connection.ServerReConnector;

public class ExecutorServer {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorServer.class);

    private DataVinesRemoteClient datavinesRemoteClient;

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

        this.datavinesRemoteClient = new DataVinesRemoteClient(new NettyClientConfig());
        this.datavinesRemoteClient.registerProcessor(CommandCode.PONG,new PongProcessor());
        this.datavinesRemoteClient.registerProcessor(CommandCode.JOB_EXECUTE_REQUEST,new JobExecuteProcessor(this.configuration,jobCallbackService));
        this.datavinesRemoteClient.registerProcessor(CommandCode.JOB_KILL_REQUEST,new JobKillProcessor());
        this.datavinesRemoteClient.registerProcessor(CommandCode.ROLL_VIEW_LOG_REQUEST,new LogRollViewProcessor());
        this.datavinesRemoteClient.registerProcessor(CommandCode.VIEW_WHOLE_LOG_REQUEST,new ViewWholeLogProcessor());
        this.datavinesRemoteClient.registerProcessor(CommandCode.GET_LOG_BYTES_REQUEST,new GetLogBytesProcessor());

        serverReConnector = new CoordinatorServerReConnector();
        serverReConnector.doConnect(this.datavinesRemoteClient, new ServerReConnector.Operator() {
            @Override
            public void execute() {

                ThreadUtils.sleep(2000);
                JobResponseCacheProcessor jobResponseCacheProcessor = new JobResponseCacheProcessor();
                jobResponseCacheProcessor.start();
            }
        }, RequestClientType.EXECUTOR);

        this.datavinesRemoteClient.addListener(new RemoveMasterChannelListener(serverReConnector));

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

            this.datavinesRemoteClient.close();

            if(serverReConnector != null){
                serverReConnector.close();
            }

        } catch (Exception e) {
            logger.error("worker server stop exception ", e);
            System.exit(-1);
        }
    }

}
