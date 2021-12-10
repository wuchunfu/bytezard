package io.simforce.bytezard.engine.api.component;

import io.simforce.bytezard.common.spi.SPI;
import io.simforce.bytezard.engine.api.env.RuntimeEnvironment;
import io.simforce.bytezard.engine.api.plugin.Plugin;

@SPI
public interface Component extends Plugin {
    void prepare(RuntimeEnvironment env);
}
