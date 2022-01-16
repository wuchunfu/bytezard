package io.simforce.bytezard.executor.runner;

import java.util.Date;
import java.util.List;

import io.simforce.bytezard.common.entity.ExecutionJob;
import io.simforce.bytezard.common.enums.ExecutionStatus;
import io.simforce.bytezard.common.spi.PluginLoader;
import io.simforce.bytezard.common.utils.LoggerUtils;
import io.simforce.bytezard.engine.api.engine.EngineExecutor;
import io.simforce.bytezard.executor.processor.JobCallbackService;
import io.simforce.bytezard.remote.command.JobExecuteResponseCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobRunner implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(JobRunner.class);

    private final ExecutionJob executionJob;

    private final JobCallbackService jobCallbackService;

    private EngineExecutor engineExecutor;

    public JobRunner(ExecutionJob executionJob, JobCallbackService jobCallbackService){
        this.jobCallbackService = jobCallbackService;
        this.executionJob = executionJob;
    }

    @Override
    public void run() {
        JobExecuteResponseCommand responseCommand =
                new JobExecuteResponseCommand(this.executionJob.getJobInstanceId());
        try{
            //下载资源文件
//            downloadResource(executionJob.getExecutePath(),
//                    executionJob.getResources(),
//                    executionJob.getTenantCode(),
//                    logger);
//            executionJob.setEnvFile(CommonUtils.getSystemEnvPath());
//            executionJob.setGlobalParameters();

            // 生成此次任务的唯一ID，platform_jobType_jobInstanceId
            executionJob.setJobUniqueId(String.format(
                    "%s_%s",
                    executionJob.getEngineType(),
                    executionJob.getJobInstanceId()));

            // custom logger
            Logger jobLogger = LoggerFactory.getLogger(
                    LoggerUtils.buildJobUniqueId(LoggerUtils.JOB_LOGGER_INFO_PREFIX,
                            executionJob.getEngineType(),
                            executionJob.getJobInstanceId()));

            engineExecutor = PluginLoader
                    .getPluginLoader(EngineExecutor.class)
                    .getNewPlugin(executionJob.getEngineType());
            engineExecutor.setExecutionJob(executionJob);
            engineExecutor.setLogger(jobLogger);

            engineExecutor.init();
            engineExecutor.execute();
            engineExecutor.after();

            if (engineExecutor.isCancel()) {
                responseCommand.setStatus(ExecutionStatus.KILL.getCode());
            }else{
                responseCommand.setStatus(engineExecutor.getProcessResult().getExitStatusCode());
            }

            responseCommand.setEndTime(new Date());
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

            responseCommand.setEndTime(new Date());
            responseCommand.setApplicationIds(engineExecutor.getProcessResult().getApplicationId());
            responseCommand.setProcessId(engineExecutor.getProcessResult().getProcessId());
        } finally {
            jobCallbackService.sendResult(executionJob.getJobInstanceId(), responseCommand.convert2Command());
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
