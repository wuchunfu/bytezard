package io.simforce.bytezard.coordinator.server.recovery.engine;

import java.util.List;

import io.simforce.bytezard.common.entity.TaskRequest;
import io.simforce.bytezard.coordinator.server.recovery.PersistenceEngine;

/**
 * @author zixi0825
 */
public class ZooKeeperPersistenceEngine implements PersistenceEngine {

    public ZooKeeperPersistenceEngine(){

    }

    @Override
    public long persist(String name, TaskRequest job) {
        return 0L;
    }

    @Override
    public void unPersist(String name) {

    }

    @Override
    public void update(String name, TaskRequest job) {

    }

    @Override
    public List<TaskRequest> getUnStartedJobs() {
        return null;
    }

    @Override
    public List<TaskRequest> getUnFinishedJobs() {
        return null;
    }

    @Override
    public TaskRequest getById(long id) {
        return null;
    }
}
