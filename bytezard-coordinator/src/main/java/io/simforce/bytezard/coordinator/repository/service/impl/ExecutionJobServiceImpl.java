package io.simforce.bytezard.coordinator.repository.service.impl;

import java.util.List;

import io.simforce.bytezard.common.entity.TaskRequest;
import io.simforce.bytezard.coordinator.repository.mapper.ExecutionJobMapper;
import io.simforce.bytezard.coordinator.repository.service.ExecutionJobService;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ExecutionJobServiceImpl implements ExecutionJobService {

    @Inject
    private ExecutionJobMapper executionJobMapper;

    @Override
    public long save(TaskRequest job){
        return executionJobMapper.save(job);
    }

    @Override
    public List<TaskRequest> getUnfinishedJobs(){

        PageInfo<TaskRequest> pageInfo = PageHelper.startPage(1, 5).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                executionJobMapper.getUnfinishedJobs();
            }
        });


        return pageInfo.getList();
    }

    @Override
    public List<TaskRequest> getUnStartedJobs(){
        Page<TaskRequest> page = PageHelper.startPage(1, 10).doSelectPage(new ISelect() {
            @Override
            public void doSelect() {
                executionJobMapper.getUnStartedJobs();
            }
        });


        return page.getResult();
    }

    @Override
    public int updateById(TaskRequest job){
        return executionJobMapper.updateById(job);
    }

    @Override
    public TaskRequest getById(long taskId){
        return executionJobMapper.selectById(taskId);
    }
}
