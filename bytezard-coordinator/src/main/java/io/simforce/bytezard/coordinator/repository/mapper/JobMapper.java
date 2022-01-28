package io.simforce.bytezard.coordinator.repository.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import io.simforce.bytezard.coordinator.repository.entity.Job;

@Mapper
public interface JobMapper extends BaseMapper<Job> {

    @Select("SELECT * from bytezard_job WHERE project_id = #{projectId} ")
    List<Job> listByProjectId(long projectId);

}
