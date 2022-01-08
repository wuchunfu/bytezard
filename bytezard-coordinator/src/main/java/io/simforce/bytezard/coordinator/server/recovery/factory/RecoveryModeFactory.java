package io.simforce.bytezard.coordinator.server.recovery.factory;

import io.simforce.bytezard.coordinator.server.recovery.PersistenceEngine;

/**
 * @author zixi0825
 */
public interface RecoveryModeFactory {

    /**
     * createPersistenceEngine
     * @return
     */
    PersistenceEngine createPersistenceEngine();

}
