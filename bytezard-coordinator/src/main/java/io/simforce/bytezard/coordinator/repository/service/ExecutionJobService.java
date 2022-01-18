package io.simforce.bytezard.coordinator.repository.service;

import java.util.List;

import io.simforce.bytezard.common.entity.TaskRequest;

public interface ExecutionJobService {

    public long save(TaskRequest job);

    public List<TaskRequest> getUnfinishedJobs();

    public List<TaskRequest> getUnStartedJobs();

    public int updateById(TaskRequest job);

    public TaskRequest getById(long taskId);
}
