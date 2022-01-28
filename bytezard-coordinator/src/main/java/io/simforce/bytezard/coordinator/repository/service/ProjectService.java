package io.simforce.bytezard.coordinator.repository.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.simforce.bytezard.coordinator.api.dto.project.ProjectCreate;
import io.simforce.bytezard.coordinator.api.dto.project.ProjectUpdate;
import io.simforce.bytezard.coordinator.repository.entity.Project;

public interface ProjectService  {

    IPage<Project> page(String keywords, Long userId, Integer pageNumber, Integer pageSize);

    List<Project> list();

    Project getById(Long id);

    Project createProject(ProjectCreate project);

    int deleteById(Long id);

    Project updateProject(ProjectUpdate project);

}
