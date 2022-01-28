package io.simforce.bytezard.coordinator.utils;

import io.simforce.bytezard.common.config.BytezardConfiguration;
import io.simforce.bytezard.common.config.EnvConfig;
import io.simforce.bytezard.common.config.SinkConfig;
import io.simforce.bytezard.common.config.SourceConfig;
import io.simforce.bytezard.common.entity.JobParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.zaxxer.hikari.HikariDataSource;

public class JobParameterParser {

    public static BytezardConfiguration parse(JobParameter jobParameter) throws Exception {
        BytezardConfiguration configuration = new BytezardConfiguration();
        EnvConfig envConfig = new EnvConfig();
        envConfig.setEngine(jobParameter.getEngineType());
        envConfig.setConfig(jobParameter.getEngineParameter());

        List<SourceConfig> sourceConfigs = new ArrayList<>();
        jobParameter.getConnectorParameter().forEach(x ->{
            SourceConfig sourceConfig = new SourceConfig();
            sourceConfig.setPlugin(x.getType());
            sourceConfig.setConfig(x.getParameters());
            sourceConfigs.add(sourceConfig);
        });

        List<SinkConfig> sinkConfigs = new ArrayList<>();
        //get the actual value storage parameter
        javax.sql.DataSource defaultDataSource =
                SpringApplicationContext.getBean(javax.sql.DataSource.class);
        HikariDataSource hikariDataSource = (HikariDataSource)defaultDataSource;
        JdbcInfo jdbcInfo = JdbcUrlParser.getJdbcInfo(hikariDataSource.getJdbcUrl());
        SinkConfig actualValueSinkConfig = new SinkConfig();
        if (jdbcInfo == null) {
            throw new Exception("");
        }
        actualValueSinkConfig.setPlugin(jdbcInfo.getDriverName());
        Map<String,Object> actualValueSinkConfigMap = new HashMap<>();
        actualValueSinkConfigMap.put("url",hikariDataSource.getJdbcUrl());
        actualValueSinkConfigMap.put("dbtable","actual_value_table");
        actualValueSinkConfigMap.put("user",hikariDataSource.getUsername());
        actualValueSinkConfigMap.put("password",hikariDataSource.getPassword());
        actualValueSinkConfig.setConfig(actualValueSinkConfigMap);

        sinkConfigs.add(actualValueSinkConfig);

        //get the error data storage parameter
        // support file(hdfs/minio/s3)/es

        //get the task data storage parameter

        configuration.setEnvConfig(envConfig);
        configuration.setSourceParameters(sourceConfigs);
        configuration.setSinkParameters(sinkConfigs);

        return configuration;
    }
}
