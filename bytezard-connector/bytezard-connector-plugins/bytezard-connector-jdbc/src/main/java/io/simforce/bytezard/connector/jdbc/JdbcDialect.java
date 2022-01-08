package io.simforce.bytezard.connector.jdbc;

import io.simforce.bytezard.connector.api.Dialect;

public class JdbcDialect implements Dialect {
    @Override
    public String getColumnPrefix() {
        return "`";
    }

    @Override
    public String getColumnSuffix() {
        return "`";
    }
}
