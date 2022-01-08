package io.simforce.bytezard.coordinator.server.recovery.factory;

import io.simforce.bytezard.coordinator.server.recovery.PersistenceEngine;
import io.simforce.bytezard.coordinator.server.recovery.engine.ZooKeeperPersistenceEngine;

public class ZooKeeperModeFactory implements RecoveryModeFactory {

    private final ZooKeeperPersistenceEngine zooKeeperPersistenceEngine;

    public ZooKeeperModeFactory(){
        this.zooKeeperPersistenceEngine = new ZooKeeperPersistenceEngine();
    }

    @Override
    public PersistenceEngine createPersistenceEngine() {
        return zooKeeperPersistenceEngine;
    }

}
