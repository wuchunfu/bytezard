package io.simforce.bytezard.engine.api.plugin;

import java.io.Serializable;

import io.simforce.bytezard.common.config.CheckResult;
import io.simforce.bytezard.common.config.Config;

public interface Plugin extends Serializable {

    void setConfig(Config config);

    Config getConfig();

    CheckResult checkConfig();
}
