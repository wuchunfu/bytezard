package io.datavines.coordinator.repository.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import io.datavines.coordinator.repository.mapper.JobMapper;
import io.datavines.coordinator.repository.service.JobService;
import io.datavines.coordinator.repository.entity.Job;

@Service("jobService")
public class JobServiceImpl extends ServiceImpl<JobMapper,Job> implements JobService {

    @Override
    public long insert(Job job) {
        baseMapper.insert(job);
        return job.getId();
    }

    @Override
    public int update(Job job) {
        return baseMapper.updateById(job);
    }

    @Override
    public Job getById(long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<Job> listByProjectId(Long projectId) {
        return baseMapper.listByProjectId(projectId);
    }

    @Override
    public int deleteById(long id) {
        return baseMapper.deleteById(id);
    }
}
