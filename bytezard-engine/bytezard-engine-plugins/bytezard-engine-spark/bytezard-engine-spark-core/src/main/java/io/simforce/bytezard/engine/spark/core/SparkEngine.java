package io.simforce.bytezard.engine.spark.core;

import io.simforce.bytezard.common.config.CheckResult;
import io.simforce.bytezard.common.config.Config;
import io.simforce.bytezard.engine.api.engine.Engine;
import io.simforce.bytezard.engine.api.env.Execution;

/**
 * @author zixi0825
 */
public class SparkEngine implements Engine {

    @Override
    public Execution getExecution() {
        return new SparkRuntimeEnvironment().getExecution();
    }

    @Override
    public void setConfig(Config config) {

    }

    @Override
    public Config getConfig() {
        return null;
    }

    @Override
    public CheckResult checkConfig() {
        return null;
    }
}
