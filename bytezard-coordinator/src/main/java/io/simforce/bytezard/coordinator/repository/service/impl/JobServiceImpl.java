package io.simforce.bytezard.coordinator.repository.service.impl;

import java.util.List;

import io.simforce.bytezard.coordinator.repository.entity.Job;
import io.simforce.bytezard.coordinator.repository.mapper.JobMapper;
import io.simforce.bytezard.coordinator.repository.service.JobService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class JobServiceImpl implements JobService {

    @Inject
    private JobMapper jobMapper;

    @Override
    public long save(Job job) {
        return jobMapper.save(job);
    }

    @Override
    public int updateById(Job job) {
        return jobMapper.updateById(job);
    }

    @Override
    public Job getById(long id) {
        return jobMapper.getById(id);
    }

    @Override
    public List<Job> listByFlowDefinitionId(Long flowDefinitionId) {
        return jobMapper.listByFlowDefinitionId(flowDefinitionId);
    }

    @Override
    public int deleteById(long id) {
        return jobMapper.deleteById(id);
    }
}
