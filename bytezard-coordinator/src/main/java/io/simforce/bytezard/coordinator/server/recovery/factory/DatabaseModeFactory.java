package io.simforce.bytezard.coordinator.server.recovery.factory;


import io.simforce.bytezard.coordinator.server.recovery.PersistenceEngine;
import io.simforce.bytezard.coordinator.server.recovery.engine.DatabasePersistenceEngine;

/**
 * @author zixi0825
 */
public class DatabaseModeFactory implements RecoveryModeFactory {

    private final DatabasePersistenceEngine databasePersistenceEngine;


    public DatabaseModeFactory(){
        this.databasePersistenceEngine = new DatabasePersistenceEngine();
    }

    @Override
    public PersistenceEngine createPersistenceEngine() {
        return databasePersistenceEngine;
    }

}
