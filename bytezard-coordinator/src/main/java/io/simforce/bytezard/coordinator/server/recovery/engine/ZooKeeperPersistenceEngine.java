package io.simforce.bytezard.coordinator.server.recovery.engine;

import java.util.List;

import io.simforce.bytezard.common.entity.ExecutionJob;
import io.simforce.bytezard.coordinator.server.recovery.PersistenceEngine;

/**
 * @author zixi0825
 */
public class ZooKeeperPersistenceEngine implements PersistenceEngine {

    public ZooKeeperPersistenceEngine(){

    }

    @Override
    public long persist(String name, ExecutionJob job) {
        return 0L;
    }

    @Override
    public void unPersist(String name) {

    }

    @Override
    public void update(String name, ExecutionJob job) {

    }

    @Override
    public List<ExecutionJob> getUnStartedJobs() {
        return null;
    }

    @Override
    public List<ExecutionJob> getUnFinishedJobs() {
        return null;
    }

    @Override
    public ExecutionJob getById(long id) {
        return null;
    }
}
