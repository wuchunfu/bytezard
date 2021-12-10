package io.simforce.bytezard.engine.plugin.spark.batch;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import io.simforce.bytezard.engine.plugin.spark.BaseSparkSource;


/**
 * SparkBatchSource
 */
public interface SparkBatchSource extends BaseSparkSource<Dataset<Row>> {
}
