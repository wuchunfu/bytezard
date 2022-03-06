package io.datavines.engine.api.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import io.datavines.engine.api.component.Component;

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
