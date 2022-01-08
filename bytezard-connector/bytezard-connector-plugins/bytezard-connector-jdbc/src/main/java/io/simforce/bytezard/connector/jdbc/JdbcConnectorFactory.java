package io.simforce.bytezard.connector.jdbc;

import io.simforce.bytezard.connector.api.Connector;
import io.simforce.bytezard.connector.api.ConnectorFactory;
import io.simforce.bytezard.connector.api.Dialect;
import io.simforce.bytezard.connector.api.ResponseConverter;

public class JdbcConnectorFactory implements ConnectorFactory {

    @Override
    public Connector getConnector() {
        return new JdbcConnector();
    }

    @Override
    public ResponseConverter getResponseConvert() {
        return new JdbcResponseConverter();
    }

    @Override
    public Dialect getDialect() {
        return new JdbcDialect();
    }
}
