package io.simforce.bytezard.engine.spark.mysql.source;

import io.simforce.bytezard.engine.spark.jdbc.source.BaseJdbcSource;

/**
 * MysqlSource
 */
public class MysqlSource extends BaseJdbcSource {

    @Override
    protected String getDriver() {
        return "com.mysql.jdbc.Driver";
    }
}
