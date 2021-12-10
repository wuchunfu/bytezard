package io.simforce.bytezard.engine.plugin.spark;

import io.simforce.bytezard.engine.api.component.Component;

/**
 * BaseSparkSource
 */
public interface BaseSparkSource<DATA> extends Component {
    DATA getData(SparkRuntimeEnvironment env);
}
