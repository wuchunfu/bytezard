package io.simforce.bytezard.engine.spark.mysql.sink;

import io.simforce.bytezard.engine.spark.jdbc.sink.BaseJdbcSink;

/**
 * MysqlSink
 */
public class MysqlSink extends BaseJdbcSink {

    @Override
    protected String getDriver() {
        return "com.mysql.jdbc.Driver";
    }
}