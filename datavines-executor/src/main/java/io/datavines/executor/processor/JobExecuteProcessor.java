package io.datavines.executor.processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.datavines.common.CommonConstants;
import io.datavines.common.entity.TaskRequest;
import io.datavines.common.enums.ExecutionStatus;
import io.datavines.common.log.JobLogDiscriminator;
import io.datavines.common.utils.FileUtils;
import io.datavines.common.utils.JSONUtils;
import io.datavines.executor.cache.JobExecutionCache;
import io.datavines.executor.cache.JobExecutionContext;
import io.datavines.executor.config.ExecutorConfiguration;
import io.datavines.executor.runner.TaskRunner;
import io.datavines.remote.command.*;
import io.datavines.remote.processor.NettyEventProcessor;
import io.datavines.remote.utils.JsonSerializer;
import io.datavines.remote.utils.NamedThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.sift.SiftingAppender;
import io.netty.channel.Channel;

public class JobExecuteProcessor implements NettyEventProcessor {

    private final Logger logger = LoggerFactory.getLogger(JobExecuteProcessor.class);

    private final ExecutorService executorService;

    private final JobCallbackService jobCallbackService;

    private final JobExecutionCache jobExecutionCache;

    public JobExecuteProcessor(ExecutorConfiguration executorConfiguration, JobCallbackService jobCallbackService){
        this.executorService = Executors.newFixedThreadPool(
                executorConfiguration.getInt(
                        ExecutorConfiguration.EXECUTOR_EXEC_THREADS,
                        ExecutorConfiguration.EXECUTOR_EXEC_THREADS_DEFAULT),
                new NamedThreadFactory("Executor-execute-thread"));
        this.jobCallbackService = jobCallbackService;
        this.jobExecutionCache = JobExecutionCache.getInstance();
    }

    @Override
    public void process(Channel channel, Command command) {

        Preconditions.checkArgument(CommandCode.JOB_EXECUTE_REQUEST == command.getCode(),
                String.format("invalid command type : %s", command.getCode()));

        JobExecuteRequestCommand jobExecuteRequestCommand
                = JsonSerializer.deserialize(command.getBody(),JobExecuteRequestCommand.class);
        //??????????????????????????????????????????jobExecutionContext
        TaskRequest taskRequest
                = JSONUtils.parseObject(jobExecuteRequestCommand.getJobExecutionContext(), TaskRequest.class);
        if(taskRequest != null){
            // local execute path
            String execLocalPath = getExecLocalPath(taskRequest);
            try {
                FileUtils.createWorkDirAndUserIfAbsent(execLocalPath, taskRequest.getTenantCode());
            } catch (Exception ex){
                logger.error(String.format("create execLocalPath : %s", execLocalPath), ex);
            }
            taskRequest.setExecuteFilePath(execLocalPath);
            Path path  = new File(execLocalPath).toPath();

            try {
                if (Files.exists(path)) {
                    Files.delete(path);
                }
            } catch(IOException e) {
                logger.info("delete path error {0}",e);
            }

            taskRequest.setStartTime(LocalDateTime.now());
            doAck(taskRequest);

            TaskRunner taskRunner = new TaskRunner(taskRequest, jobCallbackService);
            JobExecutionContext jobExecutionContext = new JobExecutionContext();
            jobExecutionContext.setTaskRequest(taskRequest);
            jobExecutionContext.setTaskRunner(taskRunner);
            jobExecutionCache.cache(jobExecutionContext);

            executorService.submit(taskRunner);
        }

    }

    private void doAck(TaskRequest taskRequest){
        JobExecuteAckCommand ackCommand = buildAckCommand(taskRequest);
        logger.info(JSONUtils.toJsonString(ackCommand));
        jobCallbackService.sendAck(taskRequest.getTaskId(),ackCommand.convert2Command());
    }

    private void doResult(TaskRequest taskRequest){
        JobExecuteResponseCommand responseCommand =
                new JobExecuteResponseCommand(taskRequest.getTaskId());
        responseCommand.setStatus(ExecutionStatus.FAILURE.getCode());
        responseCommand.setEndTime(LocalDateTime.now());
        responseCommand.setProcessId(0);
        responseCommand.setApplicationIds("");
        jobCallbackService.sendAck(taskRequest.getTaskId(),responseCommand.convert2Command());
    }

    /**
     * get execute local path
     * @param taskRequest executionJob
     * @return execute local path
     */
    private String getExecLocalPath(TaskRequest taskRequest){
        return FileUtils.getJobExecDir(
                taskRequest.getEngineType(),
                taskRequest.getTaskId());
    }

    /**
     * build ack command
     * @param taskRequest executionJob
     * @return JobExecuteAckCommand
     */
    private JobExecuteAckCommand buildAckCommand(TaskRequest taskRequest) {
        JobExecuteAckCommand ackCommand = new JobExecuteAckCommand(taskRequest.getTaskId());
        ackCommand.setStatus(ExecutionStatus.RUNNING_EXECUTION.getCode());
        ackCommand.setLogPath(getJobLogPath(taskRequest));
        ackCommand.setHost(taskRequest.getExecuteHost());
        ackCommand.setStartTime(taskRequest.getStartTime());
        if ("jdbc".equals(taskRequest.getEngineType())){
            ackCommand.setExecutePath(null);
        } else {
            ackCommand.setExecutePath(taskRequest.getExecuteFilePath());
        }
        taskRequest.setLogPath(ackCommand.getLogPath());
        return ackCommand;
    }

    /**
     * get job log path
     * @return log path
     */
    private String getJobLogPath(TaskRequest taskRequest) {
        String baseLog = ((JobLogDiscriminator) ((SiftingAppender) ((LoggerContext) LoggerFactory.getILoggerFactory())
                .getLogger("ROOT")
                .getAppender("JOB_LOG_FILE"))
                .getDiscriminator()).getLogBase();
        if (baseLog.startsWith(CommonConstants.SINGLE_SLASH)){
            return baseLog + CommonConstants.SINGLE_SLASH +
                    taskRequest.getEngineType() + CommonConstants.SINGLE_SLASH  +
                    taskRequest.getTaskId() + ".log";
        }
        return System.getProperty("user.dir") + CommonConstants.SINGLE_SLASH +
                baseLog + CommonConstants.SINGLE_SLASH +
                taskRequest.getEngineType() + CommonConstants.SINGLE_SLASH  +
                taskRequest.getTaskId() + ".log";
    }

}
