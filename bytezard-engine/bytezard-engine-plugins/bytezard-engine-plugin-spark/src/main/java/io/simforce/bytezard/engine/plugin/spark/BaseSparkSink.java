package io.simforce.bytezard.engine.plugin.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import io.simforce.bytezard.engine.api.component.Component;

/**
 * BaseSparkSink
 */
public interface BaseSparkSink<OUT> extends Component {

    /**
     * output
     * @param data
     * @param environment
     * @return
     */
    OUT output(Dataset<Row> data, SparkRuntimeEnvironment environment);
}
