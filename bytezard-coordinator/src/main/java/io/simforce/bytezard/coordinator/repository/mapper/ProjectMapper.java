package io.simforce.bytezard.coordinator.repository.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.simforce.bytezard.coordinator.repository.entity.Project;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {

    @Select("SELECT * from bytezard_project order by update_time")
    List<Project> list();

    IPage<Project> getProjectsByUserWithSearchVal(Page<Project> page,
                                                  @Param("searchVal") String searchVal,
                                                  @Param("userId") Long userId);
}
