package io.simforce.bytezard.engine.api.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import io.simforce.bytezard.engine.api.component.Component;

/**
 * BaseSparkTransform
 */
public interface BaseSparkTransform extends Component {

    Dataset<Row> process(Dataset<Row> data, SparkRuntimeEnvironment env);
}
