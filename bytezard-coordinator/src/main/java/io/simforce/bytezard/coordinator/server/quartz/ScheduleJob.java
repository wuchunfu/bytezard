/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.simforce.bytezard.coordinator.server.quartz;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import io.simforce.bytezard.common.utils.DateUtils;
import io.simforce.bytezard.common.utils.JSONUtils;
import io.simforce.bytezard.coordinator.CoordinatorConstants;
import io.simforce.bytezard.coordinator.repository.entity.Command;
import io.simforce.bytezard.coordinator.repository.entity.JobDefinition;
import io.simforce.bytezard.coordinator.repository.entity.JobInstance;
import io.simforce.bytezard.coordinator.eunms.CommandType;
import io.simforce.bytezard.coordinator.eunms.ExecutionStatus;
import io.simforce.bytezard.coordinator.repository.module.BytezardCoordinatorInjector;
import io.simforce.bytezard.coordinator.repository.service.impl.JobExternalService;

/**
 * process schedule job
 */
public class ScheduleJob implements Job {

    /**
     * logger of FlowScheduleJob
     */
    private static final Logger logger = LoggerFactory.getLogger(ScheduleJob.class);

    public JobExternalService getJobExternalService(){
        return BytezardCoordinatorInjector.getInjector().getInstance(JobExternalService.class);
    }

    /**
     * Called by the Scheduler when a Trigger fires that is associated with the Job
     *
     * @param context JobExecutionContext
     * @throws JobExecutionException if there is an exception while executing the job.
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        Long projectJobId = dataMap.getLong(CoordinatorConstants.PROJECT_JOB_ID);
        Long projectId = dataMap.getLong(CoordinatorConstants.PROJECT_ID);

        LocalDateTime scheduleTime = DateUtils.date2LocalDateTime(context.getScheduledFireTime());
        LocalDateTime fireTime = DateUtils.date2LocalDateTime(context.getFireTime());

        logger.info("scheduled fire time :{}, fire time :{}, process id :{}", scheduleTime, fireTime, projectJobId);

        JobDefinition jobDefinition = getJobExternalService().getJobDefinitionById(projectJobId);
        if (jobDefinition == null) {
            logger.warn("job {} is null", projectJobId);
            return;
        }

        JobInstance jobInstance = createJobInstance(jobDefinition,scheduleTime);

        createCommand(jobInstance,scheduleTime,fireTime);
    }

    private void deleteJob(Long projectId, long scheduleId) throws RuntimeException{
        logger.info("delete schedules of project id:{}, schedule id:{}", projectId, scheduleId);

        String jobName = QuartzExecutors.buildJobName(scheduleId);
        String jobGroupName = QuartzExecutors.buildJobGroupName(projectId);

        if(!QuartzExecutors.getInstance().deleteJob(jobName, jobGroupName)){
            logger.warn("set offline failure:projectId:{},scheduleId:{}",projectId,scheduleId);
            throw new RuntimeException("set offline failure");
        }
    }

    private JobInstance createJobInstance(JobDefinition jobDefinition, LocalDateTime scheduleTime){
        JobInstance jobInstance = new JobInstance();
        jobInstance.setName(jobDefinition.getName());
        jobInstance.setJobDefinitionId(jobDefinition.getId());
        jobInstance.setProjectId(jobDefinition.getProjectId());
        jobInstance.setParameter(jobDefinition.getParameter());
        jobInstance.setJson(jobDefinition.getJson());
        jobInstance.setStatus(ExecutionStatus.SUBMITTED_SUCCESS.getCode());
        jobInstance.setRetryTimes(jobDefinition.getRetryTimes());
        jobInstance.setRetryInterval(jobDefinition.getRetryInterval());
        jobInstance.setTimeout(jobDefinition.getTimeout());
        jobInstance.setTimeoutStrategy(jobDefinition.getTimeoutStrategy());
        jobInstance.setTenantCode(jobDefinition.getTenantCode());
        jobInstance.setEnvFile(jobDefinition.getEnvFile());
        jobInstance.setResources(jobDefinition.getResources());
        jobInstance.setSubmitTime(scheduleTime);
        jobInstance.setCreateTime(LocalDateTime.now());
        jobInstance.setUpdateTime(LocalDateTime.now());

        return jobInstance;
    }

    private void createCommand(JobInstance jobInstance, LocalDateTime scheduleTime, LocalDateTime fireTime) {
        Command command = new Command();
        command.setJobId(jobInstance.getJobDefinitionId());
        command.setType(CommandType.SCHEDULER);
        command.setPriority(1);
        command.setCreateTime(LocalDateTime.now());
        command.setSubmitTime(fireTime);
        command.setUpdateTime(LocalDateTime.now());
        command.setScheduleTime(scheduleTime);

        Map<String, String> cmdParam = null;
        if(StringUtils.isNotEmpty(command.getParameter())){
            cmdParam = JSONUtils.toMap(command.getParameter());
        }else{
            cmdParam = Maps.newHashMap();
        }
        cmdParam.put("flow_instance_id",String.valueOf(jobInstance.getId()));
        command.setParameter(JSONUtils.toJsonString(cmdParam));

        getJobExternalService().insertCommand(command);
    }
}
