package io.datavines.engine.api.spark;

import io.datavines.engine.api.component.Component;

/**
 * BaseSparkSource
 */
public interface BaseSparkSource<DATA> extends Component {
    DATA getData(SparkRuntimeEnvironment env);
}
