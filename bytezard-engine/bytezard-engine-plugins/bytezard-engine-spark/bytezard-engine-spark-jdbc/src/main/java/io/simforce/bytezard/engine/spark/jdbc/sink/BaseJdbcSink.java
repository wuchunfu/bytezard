package io.simforce.bytezard.engine.spark.jdbc.sink;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.execution.datasources.jdbc2.JDBCSaveMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import io.simforce.bytezard.common.config.CheckResult;
import io.simforce.bytezard.common.config.Config;
import io.simforce.bytezard.engine.api.env.RuntimeEnvironment;
import io.simforce.bytezard.engine.spark.core.SparkRuntimeEnvironment;
import io.simforce.bytezard.engine.spark.core.batch.SparkBatchSink;


/**
 * AbstractJdbcSink
 */
public abstract class BaseJdbcSink implements SparkBatchSink {

    private Config config = new Config();

    @Override
    public void setConfig(Config config) {
        if(config != null) {
            this.config = config;
        }
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public CheckResult checkConfig() {
        List<String> requiredOptions = Arrays.asList("url", "dbtable", "user", "password");

        List<String> nonExistsOptions = new ArrayList<>();
        requiredOptions.forEach(x->{
            if(!config.has(x)){
                nonExistsOptions.add(x);
            }
        });

        if (!nonExistsOptions.isEmpty()) {
            return new CheckResult(
                    false,
                    "please specify " + nonExistsOptions.stream().map(option ->
                            "[" + option + "]").collect(Collectors.joining(",")) + " as non-empty string");
        } else {
            return new CheckResult(true, "");
        }
    }

    @Override
    public void prepare(RuntimeEnvironment prepareEnv) {

    }

    @Override
    public Void output(Dataset<Row> data, SparkRuntimeEnvironment environment) {
        String saveMode = config.getString("save_mode");
        String customUpdateStmt = config.has("customUpdateStmt")? config.getString("customUpdateStmt") : "";
        String duplicateIncs = config.has("duplicateIncs")? config.getString("duplicateIncs") : "";
        if ("update".equals(saveMode)) {
            data.write().format("org.apache.spark.sql.execution.datasources.jdbc2").options(
                    new HashMap<String, String>(9){{
                        put("savemode", JDBCSaveMode.Update().toString());
                        put("driver",getDriver());
                        put("url",config.getString("url"));
                        put("user",config.getString("user"));
                        put("password",config.getString("password"));
                        put("useSSL","false");
                        put("duplicateIncs",duplicateIncs);
                        put("customUpdateStmt",customUpdateStmt);
                        put("showSql","true");

                    }}
            ).save();
        } else {
            Properties prop = new Properties();
            prop.setProperty("driver", getDriver());
            prop.setProperty("user", config.getString("user"));
            prop.setProperty("password", config.getString("password"));
            data.write().mode(saveMode).jdbc(config.getString("url"), config.getString("dbtable"), prop);
        }

        return null;
    }

    protected abstract String getDriver();
}
