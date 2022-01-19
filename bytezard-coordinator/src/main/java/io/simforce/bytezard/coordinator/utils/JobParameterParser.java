package io.simforce.bytezard.coordinator.utils;

import io.simforce.bytezard.common.config.BytezardConfiguration;
import io.simforce.bytezard.common.config.EnvConfig;
import io.simforce.bytezard.common.config.SourceConfig;
import io.simforce.bytezard.common.entity.JobParameter;

import java.util.ArrayList;
import java.util.List;

public class JobParameterParser {

    public static BytezardConfiguration parse(JobParameter jobParameter) {
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


        //get the actual value storage parameter

        //get the error data storage parameter
        // support file(hdfs/minio/s3)/es

        //get the task data storage parameter

        configuration.setEnvConfig(envConfig);
        configuration.setSourceParameters(sourceConfigs);

        return configuration;
    }
}
