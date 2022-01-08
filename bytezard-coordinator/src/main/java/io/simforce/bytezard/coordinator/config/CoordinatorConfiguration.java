package io.simforce.bytezard.coordinator.config;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.simforce.bytezard.common.exception.ConfigurationException;
import io.simforce.bytezard.common.utils.ConfigUtils;

/**
 * DirectoryServer 的配置和管理类
 * @author zixi0825
 */
public class CoordinatorConfiguration {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(CoordinatorConfiguration.class);

    public static final String BYTEZARD_DEPLOY_DIR = "/bytezard";
    public static final String BYTEZARD_LEADER_ELECTION_DIR = "/bytezard/leader_election";

    public static final String COORDINATOR_EXEC_THREADS = "coordinator.exec.threads";
    public static final Integer COORDINATOR_EXEC_THREADS_DEFAULT = 100;

    public static final String COORDINATOR_EXEC_JOB_NUM = "coordinator.exec.job.num";
    public static final Integer COORDINATOR_EXEC_JOB_NUM_DEFAULT = 20;

    public static final String COORDINATOR_JOB_COMMIT_RETRY_TIMES = "coordinator.job.commit.retry.times";
    public static final Integer COORDINATOR_JOB_COMMIT_RETRY_TIMES_DEFAULT = 5;

    public static final String COORDINATOR_JOB_COMMIT_INTERVAL = "coordinator.job.commit.interval";
    public static final Integer COORDINATOR_JOB_COMMIT_INTERVAL_DEFAULT = 1000;

    public static final String COORDINATOR_MAX_CPU_LOAD_AVG = "coordinator.max.cpu.load.avg";
    public static final Integer COORDINATOR_MAX_CPU_LOAD_AVG_DEFAULT = -1;

    public static final String COORDINATOR_RESERVED_MEMORY = "coordinator.reserved.memory";
    public static final Float COORDINATOR_RESERVED_MEMORY_DEFAULT = 0.3f;

    public static final String COORDINATOR_HOST_SELECT_STRATEGY = "coordinator.host.select.strategy";
    public static final String COORDINATOR_HOST_SELECT_STRATEGY_DEFAULT = "round_robin";

    public static final String COORDINATOR_LISTEN_PORT = "coordinator.listen.port";
    public static final Integer COORDINATOR_LISTEN_PORT_DEFAULT = 5677;

    public static final String COORDINATOR_SERVLET_PORT = "coordinator.servlet.port";
    public static final Integer COORDINATOR_SERVLET_PORT_DEFAULT = 9567;

    public static final String COORDINATOR_EDIT_LOG_DIR = "coordinator.edit.log.dir";
    public static final String COORDINATOR_EDIT_LOG_DIR_DEFAULT = "F:\\edits\\log";

    public static final String COORDINATOR_EDIT_LOG_BUFFER_LIMIT  = "coordinator.edit.log.buffer.limit";
    public static final Integer COORDINATOR_EDIT_LOG_BUFFER_LIMIT_DEFAULT = 25 * 1024 * 2;

    public static final String COORDINATOR_EDIT_LOG_AUTO_ROLL_THRESHOLD  = "coordinator.edit.log.auto.roll.threshold";
    public static final Integer COORDINATOR_EDIT_LOG_AUTO_ROLL_THRESHOLD_DEFAULT = 10000;

    public static final String COORDINATOR_EDIT_LOG_AUTO_ROLL_CHECK_INTERVAL_MS  = "coordinator.edit.log.auto.roll.check.interval.ms";
    public static final Integer COORDINATOR_EDIT_LOG_AUTO_ROLL_CHECK_INTERVAL_MS_DEFAULT = 3000;

    public static final String COORDINATOR_RECOVERY_MODE = "coordinator.recovery.mode";
    public static final String COORDINATOR_RECOVERY_MODE_DEFAULT = "ZOOKEEPER";

    public static final String ZOOKEEPER_QUORUM = "zookeeper.quorum";
    public static final String ZOOKEEPER_QUORUM_DEFAULT = "localhost:2181";

    public static final String SERVLET_PORT = "servlet.port";
    public static final String SERVLET_PORT_DEFAULT = "9527";

    private CoordinatorConfiguration(){}

    private static class Singleton{
        static CoordinatorConfiguration instance = new CoordinatorConfiguration();
    }

    public static CoordinatorConfiguration getInstance(){
        return Singleton.instance;
    }

    private Properties configuration;

    public void parse(String configPath) throws ConfigurationException {
        try{
            LOGGER.info("starting parse configuration file");

            configuration = ConfigUtils.loadConfigurationFile(configPath);

            //针对重要的配置进行格式的校验，出现问题则抛异常

        } catch (IllegalArgumentException e) {
            throw new ConfigurationException("parsing config file error", e);
        } catch (IOException e) {
            throw new ConfigurationException("parsing config file error 1", e);
        }
    }

    public String getString(String key){
        return configuration.getProperty(key);
    }

    public String getString(String key,String defaultValue){
        return configuration.getProperty(key,defaultValue);
    }

    public int getInt(String key){
        return Integer.parseInt(configuration.getProperty(key));
    }

    public int getInt(String key,String defaultValue) {
        return Integer.parseInt(configuration.getProperty(key,defaultValue));
    }

    public int getInt(String key,Integer defaultValue) {
        return Integer.parseInt(configuration.getProperty(key,String.valueOf(defaultValue)));
    }

    public Float getFloat(String key) {
        return Float.valueOf(configuration.getProperty(key));
    }

    public Float getFloat(String key,String defaultValue) {
        return Float.valueOf(configuration.getProperty(key,defaultValue));
    }
}
