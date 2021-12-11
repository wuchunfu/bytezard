package io.simforce.bytezard.engine.spark.core.batch;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import io.simforce.bytezard.engine.spark.core.BaseSparkSource;


/**
 * SparkBatchSource
 */
public interface SparkBatchSource extends BaseSparkSource<Dataset<Row>> {
}
