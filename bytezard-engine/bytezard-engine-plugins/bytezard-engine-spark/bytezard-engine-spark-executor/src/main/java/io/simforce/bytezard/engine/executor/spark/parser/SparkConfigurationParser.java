package io.simforce.bytezard.engine.executor.spark.parser;

import io.simforce.bytezard.common.config.BytezardConfiguration;
import io.simforce.bytezard.engine.api.parser.ConfigurationParser;

public class SparkConfigurationParser implements ConfigurationParser<BytezardConfiguration> {

    @Override
    public BytezardConfiguration parse(BytezardConfiguration bytezardConfiguration) {
        return bytezardConfiguration;
    }
}
