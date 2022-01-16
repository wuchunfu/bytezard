package io.simforce.bytezard.engine.api.spark.batch;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import io.simforce.bytezard.engine.api.spark.BaseSparkSource;

/**
 * SparkBatchSource
 */
public interface SparkBatchSource extends BaseSparkSource<Dataset<Row>> {
}
