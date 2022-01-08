package io.simforce.bytezard.coordinator.repository.service;

import java.util.List;

import io.simforce.bytezard.coordinator.repository.entity.JobDefinition;

/**
 * @author zixi0825
 */
public interface JobDefinitionService {

    /**
     * 返回主键字段id值
     * @param jobDefinition
     * @return
     */
    long save(JobDefinition jobDefinition);

    /**
     * updateById
     * @param jobDefinition
     * @return
     */
    int updateById(JobDefinition jobDefinition);

    /**
     * SELECT BY ID
     * @param id
     * @return
     */
    JobDefinition getById(long id);

    /**
     * 根据 flow definition id 获取 flow job definition 列表
     * @param flowDefinitionId
     * @return
     */
    List<JobDefinition> listByFlowDefinitionId(Long flowDefinitionId);

    /**
     * delete by id
     * @param id id
     * @return int
     */
    int deleteById(long id);
}
