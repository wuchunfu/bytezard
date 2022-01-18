package io.simforce.bytezard.coordinator.repository.service.impl;

import java.util.List;
import java.util.Map;

import io.simforce.bytezard.coordinator.repository.entity.Project;
import io.simforce.bytezard.coordinator.repository.mapper.ProjectMapper;
import io.simforce.bytezard.coordinator.repository.service.ProjectService;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ProjectServiceImpl implements ProjectService {

    @Inject
    private ProjectMapper projectMapper;

    @Override
    public PageInfo<Project> page(Map<String, Object> params) {

        Integer page = (Integer)params.remove("page");
        Integer limit = (Integer)params.remove("limit");

        if(page == null || page < 0){
            page = 0;
        }

        if(limit == null || limit < 0){
            limit = 10;
        }

        return PageHelper.startPage(page, limit).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
               list();
            }
        });
    }

    @Override
    public List<Project> list() {
        return projectMapper.list();
    }

    @Override
    public Project getById(Long id) {
        return projectMapper.getById(id);
    }

    @Override
    public Long save(Project project) {
        projectMapper.save(project);
        return project.getId();
    }

    @Override
    public int deleteById(Long id) {
        return projectMapper.deleteById(id);
    }

    @Override
    public int updateById(Project project) {
        return projectMapper.updateById(project);
    }
}
