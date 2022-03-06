package io.datavines.engine.api.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import io.datavines.engine.api.component.Component;

/**
 * BaseSparkTransform
 */
public interface BaseSparkTransform extends Component {

    Dataset<Row> process(Dataset<Row> data, SparkRuntimeEnvironment env);
}
