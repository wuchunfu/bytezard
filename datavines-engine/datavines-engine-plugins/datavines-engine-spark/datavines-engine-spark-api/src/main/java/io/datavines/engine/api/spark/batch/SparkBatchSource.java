package io.datavines.engine.api.spark.batch;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import io.datavines.engine.api.spark.BaseSparkSource;

/**
 * SparkBatchSource
 */
public interface SparkBatchSource extends BaseSparkSource<Dataset<Row>> {
}
