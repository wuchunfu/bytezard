package io.simforce.bytezard.coordinator.repository.service.impl;

import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.simforce.bytezard.common.entity.ExecutionJob;
import io.simforce.bytezard.common.utils.JSONUtils;
import io.simforce.bytezard.coordinator.repository.entity.Command;
import io.simforce.bytezard.coordinator.repository.entity.JobDefinition;
import io.simforce.bytezard.coordinator.repository.entity.JobInstance;
import io.simforce.bytezard.coordinator.repository.service.CommandService;
import io.simforce.bytezard.coordinator.repository.service.ExecutionJobService;
import io.simforce.bytezard.coordinator.repository.service.JobInstanceService;
import io.simforce.bytezard.coordinator.repository.service.JobDefinitionService;

/**
 * @author zixi0825
 */
@Singleton
public class JobExternalService {
    
    @Inject
    private JobInstanceService jobInstanceService;

    @Inject
    private CommandService commandService;

    @Inject
    private ExecutionJobService executionJobService;

    @Inject
    private JobDefinitionService jobDefinitionService;

    public JobDefinition getJobDefinitionById(Long id) {
        return jobDefinitionService.getById(id);
    }

    public JobInstance findJobInstanceById(Long id){
        return jobInstanceService.getById(id);
    }

    public JobInstance submitJob(JobInstance jobInstance){
        jobInstanceService.save(jobInstance);
        return jobInstance;
    }

    public Command getCommand(){
        return commandService.getOne();
    }

    public int deleteCommandById(long id){
        return commandService.deleteById(id);
    }

    public JobInstance executeCommand(Logger logger, int remainThread, Command command){
        Map<String, String> commandParameter = JSONUtils.toMap(command.getParameter());
        JobInstance jobInstance = jobInstanceService.getById(Long.parseLong(commandParameter.get("job_instance_id")));
        jobInstance.setStartTime(LocalDateTime.now());
        jobInstanceService.updateById(jobInstance);
        return jobInstance;
    }

    public JobInstance getJobInstanceByExecutionId(long executionId){
        return jobInstanceService.getByExecutionId(executionId);
    }

    public int updateJobInstance(JobInstance jobInstance){
        return jobInstanceService.updateById(jobInstance);
    }

    public Long insertJobInstance(JobInstance jobInstance){
        return jobInstanceService.save(jobInstance);
    }

    public Long insertCommand(Command command){
        return commandService.save(command);
    }

    public Long insertExecutionJob(ExecutionJob executionJob){
        return executionJobService.save(executionJob);
    }
}
