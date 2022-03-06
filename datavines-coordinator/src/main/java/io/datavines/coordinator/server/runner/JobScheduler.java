package io.datavines.coordinator.server.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datavines.coordinator.repository.service.impl.JobExternalService;
import io.datavines.coordinator.server.cache.JobExecuteManager;
import io.datavines.coordinator.utils.SpringApplicationContext;
import io.datavines.common.entity.TaskRequest;
import io.datavines.common.utils.JSONUtils;
import io.datavines.common.utils.Stopper;
import io.datavines.common.utils.ThreadUtils;
import io.datavines.coordinator.repository.entity.Command;
import io.datavines.coordinator.repository.entity.Task;

public class JobScheduler extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(JobScheduler.class);

    private final JobExternalService jobExternalService;

    private final JobExecuteManager jobExecuteManager;

    public JobScheduler(JobExecuteManager jobExecuteManager){
        this.jobExternalService = SpringApplicationContext.getBean(JobExternalService.class);
        this.jobExecuteManager = jobExecuteManager;
    }

    @Override
    public void run() {
        logger.info("job scheduler started");
        while (Stopper.isRunning()) {
            try {
                Command command = jobExternalService.getCommand();
                if(command != null){
                    Task task = jobExternalService.executeCommand(logger,100-1,command);

                    if (task != null) {
                        logger.info("start submit job : {} ",JSONUtils.toJsonString(task));
                        TaskRequest taskRequest = jobExternalService.buildTaskRequest(task);
                        boolean result = jobExecuteManager.addExecuteCommand(taskRequest);
                        if (result) {
                            jobExternalService.deleteCommandById(command.getId());
                        }
                        logger.info(String.format("submit success, job : %s", task.getName()) );
                    }

                    ThreadUtils.sleep(1000);
                } else {
                    ThreadUtils.sleep(2000);
                }
            } catch (Exception e){
                logger.error("schedule job error ",e);
            }
        }
    }

}
