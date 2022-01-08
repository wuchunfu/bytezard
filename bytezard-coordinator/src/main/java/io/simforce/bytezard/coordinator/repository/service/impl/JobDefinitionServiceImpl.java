package io.simforce.bytezard.coordinator.repository.service.impl;

import java.util.List;

import io.simforce.bytezard.coordinator.repository.entity.JobDefinition;
import io.simforce.bytezard.coordinator.repository.mapper.JobDefinitionMapper;
import io.simforce.bytezard.coordinator.repository.service.JobDefinitionService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author zixi0825
 */
@Singleton
public class JobDefinitionServiceImpl implements JobDefinitionService {

    @Inject
    private JobDefinitionMapper jobDefinitionMapper;

    @Override
    public long save(JobDefinition jobDefinition) {
        return jobDefinitionMapper.save(jobDefinition);
    }

    @Override
    public int updateById(JobDefinition jobDefinition) {
        return jobDefinitionMapper.updateById(jobDefinition);
    }

    @Override
    public JobDefinition getById(long id) {
        return jobDefinitionMapper.getById(id);
    }

    @Override
    public List<JobDefinition> listByFlowDefinitionId(Long flowDefinitionId) {
        return jobDefinitionMapper.listByFlowDefinitionId(flowDefinitionId);
    }

    @Override
    public int deleteById(long id) {
        return jobDefinitionMapper.deleteById(id);
    }
}
