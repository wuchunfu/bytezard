package io.simforce.bytezard.executor.processor;

import io.simforce.bytezard.remote.connection.ServerReConnector;

public class CoordinatorServerReConnector extends ServerReConnector {

    @Override
    public String getActiveNode() {
        return registryCenter.getActiveMaster();
    }
}
