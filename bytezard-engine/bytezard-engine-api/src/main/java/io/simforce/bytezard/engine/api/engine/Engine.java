package io.simforce.bytezard.engine.api.engine;

import io.simforce.bytezard.common.spi.SPI;
import io.simforce.bytezard.engine.api.env.Execution;
import io.simforce.bytezard.engine.api.plugin.Plugin;

@SPI
public interface Engine extends Plugin {

    Execution getExecution();
}
