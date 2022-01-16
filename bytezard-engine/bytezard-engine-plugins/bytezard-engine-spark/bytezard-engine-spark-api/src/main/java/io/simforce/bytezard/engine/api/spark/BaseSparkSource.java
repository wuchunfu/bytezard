package io.simforce.bytezard.engine.api.spark;

import io.simforce.bytezard.engine.api.component.Component;

/**
 * BaseSparkSource
 */
public interface BaseSparkSource<DATA> extends Component {
    DATA getData(SparkRuntimeEnvironment env);
}
