package io.datavines.engine.api.parser;

import io.datavines.common.config.DataVinesConfiguration;
import io.datavines.spi.SPI;;

@SPI
public interface ConfigurationParser<T> {

    T parse(DataVinesConfiguration datavinesConfiguration);
}
