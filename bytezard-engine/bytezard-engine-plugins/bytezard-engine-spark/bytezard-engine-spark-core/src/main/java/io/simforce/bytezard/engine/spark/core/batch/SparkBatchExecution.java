package io.simforce.bytezard.engine.spark.core.batch;

import static io.simforce.bytezard.engine.api.Constants.RESULT_TABLE_NAME;
import static io.simforce.bytezard.engine.api.Constants.SOURCE_TABLE_NAME;
import static io.simforce.bytezard.engine.api.Constants.TMP_TABLE_NAME;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.List;

import io.simforce.bytezard.common.config.Config;
import io.simforce.bytezard.common.config.ConfigRuntimeException;
import io.simforce.bytezard.engine.api.env.Execution;
import io.simforce.bytezard.engine.spark.core.BaseSparkSource;
import io.simforce.bytezard.engine.spark.core.BaseSparkTransform;
import io.simforce.bytezard.engine.spark.core.SparkRuntimeEnvironment;

/**
 * SparkBatchExecution
 */
public class SparkBatchExecution implements Execution<SparkBatchSource, BaseSparkTransform, SparkBatchSink> {

    private final SparkRuntimeEnvironment environment;

    public SparkBatchExecution(SparkRuntimeEnvironment environment) throws ConfigRuntimeException {
        this.environment = environment;
    }

    @Override
    public void execute(List<SparkBatchSource> sources, List<BaseSparkTransform> transforms, List<SparkBatchSink> sinks) {
        sources.forEach(s -> {
                registerInputTempView(s, environment);
        });

        if (!sources.isEmpty()) {
            Dataset<Row> ds = sources.get(0).getData(environment);
            for (BaseSparkTransform tf:transforms) {
                if (ds.takeAsList(1).size() > 0) {
                    ds = transformProcess(environment, tf, ds);
                    registerTransformTempView(tf, ds);
                }
            }

            for(SparkBatchSink sink: sinks) {
                sinkProcess(environment, sink, ds);
            }
        }
    }

    private void registerTempView(String tableName, Dataset<Row> ds) {
        ds.createOrReplaceTempView(tableName);
    }

    private void registerInputTempView(BaseSparkSource<Dataset<Row>> source, SparkRuntimeEnvironment environment) {
        Config conf = source.getConfig();
        if (conf.has(RESULT_TABLE_NAME)) {
            String tableName = conf.getString(RESULT_TABLE_NAME);
            registerTempView(tableName, source.getData(environment));
        } else {
            throw new ConfigRuntimeException(
                    "Plugin[" + source.getClass().getName() + "] must be registered as dataset/table, please set \"result_table_name\" config");
        }
    }

    private Dataset<Row> transformProcess(SparkRuntimeEnvironment environment, BaseSparkTransform transform, Dataset<Row> ds) {
        Config config = transform.getConfig();
        Dataset<Row> fromDs;
        Dataset<Row> resultDs = null;
        if (config.has(SOURCE_TABLE_NAME)) {
            String[] tableNames = config.getString(SOURCE_TABLE_NAME).split(",");

            for (String sourceTableName: tableNames) {
                fromDs = environment.sparkSession().read().table(sourceTableName);

                if(resultDs == null) {
                    resultDs = fromDs;
                } else {
                    resultDs = resultDs.union(fromDs);
                }
            }
        } else {
            resultDs = ds;
        }

        if (config.has(TMP_TABLE_NAME)) {
            if(resultDs == null) {
                resultDs = ds;
            }
            String tableName = config.getString(TMP_TABLE_NAME);
            registerTempView(tableName, resultDs);
        }

        return transform.process(resultDs, environment);
    }

    private void registerTransformTempView(BaseSparkTransform plugin, Dataset<Row> ds) {
        Config config = plugin.getConfig();
        if (config.has(RESULT_TABLE_NAME)) {
            String tableName = config.getString(RESULT_TABLE_NAME);
            registerTempView(tableName, ds);
        }
    }

    private void sinkProcess(SparkRuntimeEnvironment environment, SparkBatchSink sink, Dataset<Row> ds) {
        Config config = sink.getConfig();
        Dataset<Row> fromDs = ds;
        if (config.has(SOURCE_TABLE_NAME)) {
            String sourceTableName = config.getString(SOURCE_TABLE_NAME);
            fromDs = environment.sparkSession().read().table(sourceTableName);
        }
        sink.output(fromDs, environment);
    }
}
