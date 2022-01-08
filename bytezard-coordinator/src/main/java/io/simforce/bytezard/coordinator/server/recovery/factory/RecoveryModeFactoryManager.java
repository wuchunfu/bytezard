package io.simforce.bytezard.coordinator.server.recovery.factory;

/**
 * @author zixi0825
 */

public class RecoveryModeFactoryManager {

    private final ZooKeeperModeFactory zooKeeperModeFactory;
    private final DatabaseModeFactory databaseModeFactory;

    public RecoveryModeFactoryManager(){
        zooKeeperModeFactory = new ZooKeeperModeFactory();
        databaseModeFactory = new DatabaseModeFactory();
    }

    public RecoveryModeFactory getRecoveryModeFactory(String mode){
        switch (mode){
            case "DATABASE":
                return databaseModeFactory;
            case "ZOOKEEPER":
                return zooKeeperModeFactory;
            case "FILE_SYSTEM":
                break;
            case "NONE":
                break;
                default:
        }

        return null;
    }
}
