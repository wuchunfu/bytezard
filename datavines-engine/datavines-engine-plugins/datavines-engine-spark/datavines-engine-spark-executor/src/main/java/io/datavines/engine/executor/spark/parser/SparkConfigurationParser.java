package io.datavines.engine.executor.spark.parser;

import io.datavines.common.config.DataVinesConfiguration;
import io.datavines.engine.api.parser.ConfigurationParser;

public class SparkConfigurationParser implements ConfigurationParser<DataVinesConfiguration> {

    @Override
    public DataVinesConfiguration parse(DataVinesConfiguration dataVinesConfiguration) {
        return dataVinesConfiguration;
    }
}
