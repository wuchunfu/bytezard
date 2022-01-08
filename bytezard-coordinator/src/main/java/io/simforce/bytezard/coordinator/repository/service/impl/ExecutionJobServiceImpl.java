package io.simforce.bytezard.coordinator.repository.service.impl;

import java.util.List;

import io.simforce.bytezard.common.entity.ExecutionJob;
import io.simforce.bytezard.coordinator.repository.mapper.ExecutionJobMapper;
import io.simforce.bytezard.coordinator.repository.service.ExecutionJobService;

import com.github.pagehelper.ISelect;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author zixi0825
 */
@Singleton
public class ExecutionJobServiceImpl implements ExecutionJobService {

    @Inject
    private ExecutionJobMapper executionJobMapper;

    @Override
    public long save(ExecutionJob job){
        return executionJobMapper.save(job);
    }

    @Override
    public List<ExecutionJob> getUnfinishedJobs(){

        PageInfo<ExecutionJob> pageInfo = PageHelper.startPage(1, 5).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                executionJobMapper.getUnfinishedJobs();
            }
        });


        return pageInfo.getList();
    }

    @Override
    public List<ExecutionJob> getUnStartedJobs(){
        Page<ExecutionJob> page = PageHelper.startPage(1, 10).doSelectPage(new ISelect() {
            @Override
            public void doSelect() {
                executionJobMapper.getUnStartedJobs();
            }
        });


        return page.getResult();
    }

    @Override
    public int updateById(ExecutionJob job){
        return executionJobMapper.updateById(job);
    }

    @Override
    public ExecutionJob getById(long jobInstanceId){
        return executionJobMapper.selectById(jobInstanceId);
    }
}
