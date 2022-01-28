package io.simforce.bytezard.coordinator.repository.service.impl;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import io.simforce.bytezard.coordinator.api.dto.project.ProjectCreate;
import io.simforce.bytezard.coordinator.api.dto.project.ProjectUpdate;
import io.simforce.bytezard.coordinator.api.enums.BytezardApiException;
import io.simforce.bytezard.coordinator.api.enums.Status;
import io.simforce.bytezard.coordinator.repository.entity.Project;
import io.simforce.bytezard.coordinator.repository.mapper.ProjectMapper;
import io.simforce.bytezard.coordinator.repository.service.ProjectService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

@Service("projectService")
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    @Override
    public IPage<Project> page(String keywords, Long userId, Integer pageNumber, Integer pageSize) {
        Page<Project> page = new Page<>(pageNumber, pageSize);
        return baseMapper.getProjectsByUserWithSearchVal(page, keywords, userId);
    }

    @Override
    public List<Project> list() {
        return baseMapper.list();
    }

    @Override
    public Project getById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public Project createProject(ProjectCreate projectCreate) {
        if (isExist(projectCreate.getName())) {
            throw new BytezardApiException(Status.PROJECT_IS_EXIST_ERROR, projectCreate.getName());
        }

        Project project = new Project();
        BeanUtils.copyProperties(projectCreate, project);

        if (baseMapper.insert(project) < 1) {
            throw new BytezardApiException(Status.PROJECT_CREATE_ERROR, project.getName());
        }
        return project;
    }

    @Override
    public int deleteById(Long id) {
        return baseMapper.deleteById(id);
    }

    @Override
    public Project updateProject(ProjectUpdate projectUpdate) {
        Project project = baseMapper.selectById(projectUpdate.getId());
        if (project != null) {
            //
        } else {
            throw new BytezardApiException(Status.PROJECT_IS_NOT_EXIST_ERROR, projectUpdate.getName());
        }

        BeanUtils.copyProperties(projectUpdate, project);

        if (baseMapper.updateById(project) < 1) {
            throw new BytezardApiException(Status.PROJECT_UPDATE_ERROR, project.getName());
        }

        return project;
    }

    public boolean isExist(String search) {
        return baseMapper.selectOne(new QueryWrapper<Project>().eq("name", search)) != null;
    }
}
