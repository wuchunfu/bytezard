package io.datavines.coordinator.repository.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;

import io.datavines.coordinator.api.dto.project.ProjectCreate;
import io.datavines.coordinator.api.dto.project.ProjectUpdate;
import io.datavines.coordinator.repository.entity.Project;

public interface ProjectService  {

    IPage<Project> page(String keywords, Long userId, Integer pageNumber, Integer pageSize);

    List<Project> list();

    Project getById(Long id);

    Project createProject(ProjectCreate project);

    int deleteById(Long id);

    Project updateProject(ProjectUpdate project);

}
