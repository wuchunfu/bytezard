package io.simforce.bytezard.executor.runner;

import java.time.LocalDateTime;
import java.util.List;

import io.simforce.bytezard.common.entity.TaskRequest;
import io.simforce.bytezard.common.enums.ExecutionStatus;
import io.simforce.bytezard.common.spi.PluginLoader;
import io.simforce.bytezard.common.utils.LoggerUtils;
import io.simforce.bytezard.engine.api.engine.EngineExecutor;
import io.simforce.bytezard.executor.processor.JobCallbackService;
import io.simforce.bytezard.remote.command.JobExecuteResponseCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskRunner implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(TaskRunner.class);

    private final TaskRequest taskRequest;

    private final JobCallbackService jobCallbackService;

    private EngineExecutor engineExecutor;

    public TaskRunner(TaskRequest taskRequest, JobCallbackService jobCallbackService){
        this.jobCallbackService = jobCallbackService;
        this.taskRequest = taskRequest;
    }

    @Override
    public void run() {
        JobExecuteResponseCommand responseCommand =
                new JobExecuteResponseCommand(this.taskRequest.getTaskId());
        try{
            //下载资源文件
//            downloadResource(executionJob.getExecutePath(),
//                    executionJob.getResources(),
//                    executionJob.getTenantCode(),
//                    logger);
//            executionJob.setEnvFile(CommonUtils.getSystemEnvPath());
//            executionJob.setGlobalParameters();

            // 生成此次任务的唯一ID，platform_jobType_taskId
//            taskRequest.setJobUniqueId(String.format(
//                    "%s_%s",
//                    taskRequest.getEngineType(),
//                    taskRequest.getTaskId()));

            // custom logger
            Logger jobLogger = LoggerFactory.getLogger(
                    LoggerUtils.buildJobUniqueId(LoggerUtils.JOB_LOGGER_INFO_PREFIX,
                            taskRequest.getEngineType(),
                            taskRequest.getTaskId()));

            engineExecutor = PluginLoader
                    .getPluginLoader(EngineExecutor.class)
                    .getNewPlugin(taskRequest.getEngineType());
            engineExecutor.setTaskRequest(taskRequest);
            engineExecutor.setLogger(jobLogger);

            engineExecutor.init();
            engineExecutor.execute();
            engineExecutor.after();

            if (engineExecutor.isCancel()) {
                responseCommand.setStatus(ExecutionStatus.KILL.getCode());
            }else{
                responseCommand.setStatus(engineExecutor.getProcessResult().getExitStatusCode());
            }

            responseCommand.setEndTime(LocalDateTime.now());
            responseCommand.setApplicationIds(engineExecutor.getProcessResult().getApplicationId());
            responseCommand.setProcessId(engineExecutor.getProcessResult().getProcessId());

        } catch (Exception e) {
            logger.error("job scheduler failure", e);
            kill();
            try {
                if (engineExecutor.isCancel()) {
                    responseCommand.setStatus(ExecutionStatus.KILL.getCode());
                } else {
                    responseCommand.setStatus(ExecutionStatus.FAILURE.getCode());
                }
            } catch (Exception ex) {
                logger.error("job scheduler failure", ex);
            }

            responseCommand.setEndTime(LocalDateTime.now());
            responseCommand.setApplicationIds(engineExecutor.getProcessResult().getApplicationId());
            responseCommand.setProcessId(engineExecutor.getProcessResult().getProcessId());
        } finally {
            jobCallbackService.sendResult(taskRequest.getTaskId(), responseCommand.convert2Command());
        }
    }


    /**
     * download resource file
     *
     * @param execLocalPath
     * @param projectRes
     * @param logger
     */
    private void downloadResource(String execLocalPath,
                                  List<String> projectRes,
                                  String tenantCode,
                                  Logger logger) throws Exception {
        //判断资源是否为空
        //如果文件存在则删除
        //下载文件
    }

    /**
     *  kill job
     */
    public void kill(){
        if (engineExecutor != null) {
            try {
                engineExecutor.cancel();
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }
        }
    }
}
