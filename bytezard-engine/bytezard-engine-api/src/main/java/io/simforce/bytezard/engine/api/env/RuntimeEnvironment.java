package io.simforce.bytezard.engine.api.env;

import io.simforce.bytezard.common.spi.SPI;
import io.simforce.bytezard.engine.api.plugin.Plugin;

@SPI
public interface RuntimeEnvironment extends Plugin {

    void prepare();

    Execution getExecution();
}
