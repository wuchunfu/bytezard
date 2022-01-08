package io.simforce.bytezard.coordinator.repository.service.impl;

import java.util.List;

import io.simforce.bytezard.coordinator.repository.entity.JobInstance;
import io.simforce.bytezard.coordinator.repository.mapper.JobInstanceMapper;
import io.simforce.bytezard.coordinator.repository.service.JobInstanceService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author zixi0825
 */
@Singleton
public class JobInstanceServiceImpl implements JobInstanceService {

    @Inject
    private JobInstanceMapper jobInstanceMapper;

    @Override
    public long save(JobInstance jobInstance) {
        jobInstanceMapper.save(jobInstance);
        return jobInstance.getId();
    }

    @Override
    public int updateById(JobInstance jobInstance) {
        return jobInstanceMapper.updateById(jobInstance);
    }

    @Override
    public JobInstance getById(long id) {
        return jobInstanceMapper.getById(id);
    }

    @Override
    public JobInstance getByExecutionId(long executionId) {
        return jobInstanceMapper.getByExecutionId(executionId);
    }

    @Override
    public List<JobInstance> listByProjectId(long projectId) {
        return jobInstanceMapper.listByProjectId(projectId);
    }
}
