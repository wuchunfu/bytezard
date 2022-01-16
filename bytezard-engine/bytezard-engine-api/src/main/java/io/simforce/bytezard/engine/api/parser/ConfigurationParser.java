package io.simforce.bytezard.engine.api.parser;

import io.simforce.bytezard.common.config.BytezardConfiguration;
import io.simforce.bytezard.common.spi.SPI;

@SPI
public interface ConfigurationParser<T> {

    T parse(BytezardConfiguration bytezardConfiguration);
}
