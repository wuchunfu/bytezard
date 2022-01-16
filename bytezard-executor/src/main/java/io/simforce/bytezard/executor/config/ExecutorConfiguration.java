package io.simforce.bytezard.executor.config;

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
public class ExecutorConfiguration {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ExecutorConfiguration.class);

    public static final String EXECUTOR_EXEC_THREADS = "executor.exec.threads";
    public static final Integer EXECUTOR_EXEC_THREADS_DEFAULT = 100;

    public static final String EXECUTOR_MAX_CPU_LOAD_AVG = "executor.max.cpu.load.avg";
    public static final Integer EXECUTOR_MAX_CPU_LOAD_AVG_DEFAULT = -1;

    public static final String EXECUTOR_RESERVED_MEMORY = "executor.reserved.memory";
    public static final Float EXECUTOR_RESERVED_MEMORY_DEFAULT = 0.3f;

    public static final String EXECUTOR_LISTEN_PORT = "executor.listen.port";
    public static final Integer EXECUTOR_LISTEN_PORT_DEFAULT = 5677;

    public static final String ZOOKEEPER_QUORUM = "zookeeper.quorum";
    public static final String ZOOKEEPER_QUORUM_DEFAULT = "localhost:2181";

    private ExecutorConfiguration(){}

    private static class Singleton{
        static ExecutorConfiguration instance = new ExecutorConfiguration();
    }

    public static ExecutorConfiguration getInstance(){
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

    public int getInt(String key,String defaultValue){
        return Integer.parseInt(configuration.getProperty(key,defaultValue));
    }

    public int getInt(String key,Integer defaultValue){
        return Integer.parseInt(configuration.getProperty(key,String.valueOf(defaultValue)));
    }

    public Float getFloat(String key){
        return Float.valueOf(configuration.getProperty(key));
    }

    public Float getFloat(String key,String defaultValue){
        return Float.valueOf(configuration.getProperty(key,defaultValue));
    }
}
