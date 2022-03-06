package io.datavines.executor.processor;

import io.datavines.remote.connection.ServerReConnector;

public class CoordinatorServerReConnector extends ServerReConnector {

    @Override
    public String getActiveNode() {
        return registryCenter.getActiveMaster();
    }
}
