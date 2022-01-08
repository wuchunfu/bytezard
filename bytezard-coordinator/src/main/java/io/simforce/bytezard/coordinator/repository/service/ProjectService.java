package io.simforce.bytezard.coordinator.repository.service;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageInfo;

import io.simforce.bytezard.coordinator.repository.entity.Project;

/**
 * @author zixi0825
 */
public interface ProjectService  {

    PageInfo<Project> page(Map<String, Object> params);

    List<Project> list();

    Project getById(Long id);

    Long save(Project project);

    /**
     * delete by id
     * @param id id
     * @return int
     */
    int deleteById(Long id);

    int updateById(Project project);

}
