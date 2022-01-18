package io.simforce.bytezard.coordinator.server.recovery.engine;

import java.util.List;

import com.google.inject.Injector;

import io.simforce.bytezard.common.entity.TaskRequest;
import io.simforce.bytezard.coordinator.repository.module.BytezardCoordinatorInjector;
import io.simforce.bytezard.coordinator.server.recovery.PersistenceEngine;
import io.simforce.bytezard.coordinator.repository.service.ExecutionJobService;

/**
 * @author zixi0825
 */
public class DatabasePersistenceEngine implements PersistenceEngine {

    private final ExecutionJobService executionJobService;

    public DatabasePersistenceEngine(){
        Injector injector = BytezardCoordinatorInjector.getInjector();
        executionJobService = injector.getInstance(ExecutionJobService.class);
    }

    @Override
    public long persist(String name, TaskRequest job) {
        return 0;
    }

    @Override
    public void unPersist(String name) {

    }

    @Override
    public void update(String name, TaskRequest job) {
        this.executionJobService.updateById(job);
    }

    @Override
    public List<TaskRequest> getUnStartedJobs() {
        return this.executionJobService.getUnStartedJobs();
    }

    @Override
    public List<TaskRequest> getUnFinishedJobs() {
        return this.executionJobService.getUnfinishedJobs();

    }

    @Override
    public TaskRequest getById(long id) {
        return this.executionJobService.getById(id);
    }
}
