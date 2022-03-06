package io.datavines.coordinator.repository.service.impl;

import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import io.datavines.coordinator.repository.service.CommandService;
import io.datavines.coordinator.repository.service.JobService;
import io.datavines.coordinator.repository.service.TaskService;
import io.datavines.common.entity.TaskRequest;
import io.datavines.common.utils.JSONUtils;
import io.datavines.coordinator.repository.entity.Command;
import io.datavines.coordinator.repository.entity.Job;
import io.datavines.coordinator.repository.entity.Task;

@Component
public class JobExternalService {
    
    @Autowired
    private TaskService taskService;

    @Autowired
    private CommandService commandService;

    @Autowired
    private JobService jobService;

    public Job getJobById(Long id) {
        return jobService.getById(id);
    }

    public Task getTaskById(Long id){
        return taskService.getById(id);
    }

    public Task submitJob(Task task){
        taskService.insert(task);
        return task;
    }

    public Command getCommand(){
        return commandService.getOne();
    }

    public int deleteCommandById(long id){
        return commandService.deleteById(id);
    }

    public Task executeCommand(Logger logger, int remainThread, Command command){
        Map<String, String> commandParameter = JSONUtils.toMap(command.getParameter());
        Task task = taskService.getById(Long.parseLong(commandParameter.get("task_id")));
        task.setStartTime(LocalDateTime.now());
        taskService.update(task);
        return task;
    }

    public int updateTask(Task task){
        return taskService.update(task);
    }

    public Long insertTask(Task task){
        return taskService.insert(task);
    }

    public Long insertCommand(Command command){
        return commandService.insert(command);
    }

    public void updateTaskStatus(Long taskId, int status){
        Task task = getTaskById(taskId);
        task.setStatus(status);
        updateTask(task);
    }

    public void updateTaskRetryTimes(Long taskId, int times) {
        Task task = getTaskById(taskId);
        task.setRetryTimes(times);
        updateTask(task);
    }

    public TaskRequest buildTaskRequest(Task task){
        // need to convert job parameter to other parameter
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTaskName(task.getName());
//        taskRequest(task.getParameter());
////        executionJob.setJobType(task.getType());
//        taskRequest.setTenantCode(task.getTenantCode());
//        taskRequest.setEnvFile(task.getEnvFile());
//        taskRequest.setResources(task.getResources());
//        taskRequest.setTimeout(task.getTimeout());
        return taskRequest;
    }

}
