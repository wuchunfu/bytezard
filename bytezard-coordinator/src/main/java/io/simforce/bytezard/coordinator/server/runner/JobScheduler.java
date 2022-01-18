package io.simforce.bytezard.coordinator.server.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.simforce.bytezard.common.entity.TaskRequest;
import io.simforce.bytezard.common.utils.JSONUtils;
import io.simforce.bytezard.common.utils.Stopper;
import io.simforce.bytezard.common.utils.ThreadUtils;
import io.simforce.bytezard.coordinator.repository.entity.Command;
import io.simforce.bytezard.coordinator.repository.entity.Task;
import io.simforce.bytezard.coordinator.repository.module.BytezardCoordinatorInjector;
import io.simforce.bytezard.coordinator.repository.service.impl.JobExternalService;
import io.simforce.bytezard.coordinator.server.cache.JobExecuteManager;

public class JobScheduler extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(JobScheduler.class);

    private final JobExternalService jobExternalService;

    private final JobExecuteManager jobExecuteManager;

    public JobScheduler(JobExecuteManager jobExecuteManager){
        this.jobExternalService = BytezardCoordinatorInjector
                .getInjector()
                .getInstance(JobExternalService.class);
        this.jobExecuteManager = jobExecuteManager;
    }

    @Override
    public void run() {
        logger.info("job scheduler started");
        while (Stopper.isRunning()) {
            try {
                Command command = jobExternalService.getCommand();
                logger.info(JSONUtils.toJsonString(command));
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
