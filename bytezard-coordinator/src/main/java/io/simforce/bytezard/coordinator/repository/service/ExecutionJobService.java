package io.simforce.bytezard.coordinator.repository.service;

import java.util.List;

import io.simforce.bytezard.common.entity.ExecutionJob;

/**
 * @author zixi0825
 */
public interface ExecutionJobService {

    public long save(ExecutionJob job);

    public List<ExecutionJob> getUnfinishedJobs();

    public List<ExecutionJob> getUnStartedJobs();

    public int updateById(ExecutionJob job);

    public ExecutionJob getById(long jobInstanceId);
}
