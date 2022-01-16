package io.simforce.bytezard.executor.processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.simforce.bytezard.common.CommonConstants;
import io.simforce.bytezard.common.entity.ExecutionJob;
import io.simforce.bytezard.common.enums.ExecutionStatus;
import io.simforce.bytezard.common.log.JobLogDiscriminator;
import io.simforce.bytezard.common.utils.FileUtils;
import io.simforce.bytezard.common.utils.JSONUtils;
import io.simforce.bytezard.executor.cache.JobExecutionCache;
import io.simforce.bytezard.executor.cache.JobExecutionContext;
import io.simforce.bytezard.executor.config.ExecutorConfiguration;
import io.simforce.bytezard.executor.runner.JobRunner;
import io.simforce.bytezard.remote.command.*;
import io.simforce.bytezard.remote.processor.NettyEventProcessor;
import io.simforce.bytezard.remote.utils.FastJsonSerializer;
import io.simforce.bytezard.remote.utils.NamedThreadFactory;

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
                = FastJsonSerializer.deserialize(command.getBody(),JobExecuteRequestCommand.class);
        //拿到命令，构建一些属性添加到jobExecutionContext
        ExecutionJob executionJob
                = JSONUtils.parseObject(jobExecuteRequestCommand.getJobExecutionContext(),ExecutionJob.class);
        if(executionJob != null){
            // local execute path
            String execLocalPath = getExecLocalPath(executionJob);
            try {
                FileUtils.createWorkDirAndUserIfAbsent(execLocalPath, executionJob.getTenantCode());
            } catch (Exception ex){
                logger.error(String.format("create execLocalPath : %s", execLocalPath), ex);
            }
            executionJob.setExecutePath(execLocalPath);
            Path path  = new File(execLocalPath).toPath();

            try {
                if (Files.exists(path)) {
                    Files.delete(path);
                }
            } catch(IOException e) {
                logger.info("delete path error {0}",e);
            }

            executionJob.setStartTime(new Date());
            doAck(executionJob);

            JobRunner jobRunner = new JobRunner(executionJob, jobCallbackService);
            JobExecutionContext jobExecutionContext = new JobExecutionContext();
            jobExecutionContext.setExecutionJob(executionJob);
            jobExecutionContext.setJobRunner(jobRunner);
            jobExecutionCache.cache(jobExecutionContext);

            executorService.submit(jobRunner);
        }

    }

    private void doAck(ExecutionJob executionJob){
        JobExecuteAckCommand ackCommand = buildAckCommand(executionJob);
        logger.info(JSONUtils.toJsonString(ackCommand));
        jobCallbackService.sendAck(executionJob.getJobInstanceId(),ackCommand.convert2Command());
    }

    private void doResult(ExecutionJob executionJob){
        JobExecuteResponseCommand responseCommand =
                new JobExecuteResponseCommand(executionJob.getJobInstanceId());
        responseCommand.setStatus(ExecutionStatus.FAILURE.getCode());
        responseCommand.setEndTime(new Date());
        responseCommand.setProcessId(0);
        responseCommand.setApplicationIds("");
        jobCallbackService.sendAck(executionJob.getJobInstanceId(),responseCommand.convert2Command());
    }

    /**
     * get execute local path
     * @param executionJob executionJob
     * @return execute local path
     */
    private String getExecLocalPath(ExecutionJob executionJob){
        return FileUtils.getJobExecDir(
                executionJob.getEngineType(),
                executionJob.getJobInstanceId());
    }

    /**
     * build ack command
     * @param executionJob executionJob
     * @return JobExecuteAckCommand
     */
    private JobExecuteAckCommand buildAckCommand(ExecutionJob executionJob) {
        JobExecuteAckCommand ackCommand = new JobExecuteAckCommand(executionJob.getJobInstanceId());
        ackCommand.setStatus(ExecutionStatus.RUNNING_EXECUTION.getCode());
        ackCommand.setLogPath(getJobLogPath(executionJob));
        ackCommand.setHost(executionJob.getExecuteHost());
        ackCommand.setStartTime(executionJob.getStartTime());
        if ("jdbc".equals(executionJob.getEngineType())){
            ackCommand.setExecutePath(null);
        } else {
            ackCommand.setExecutePath(executionJob.getExecutePath());
        }
        executionJob.setLogPath(ackCommand.getLogPath());
        return ackCommand;
    }

    /**
     * get job log path
     * @return log path
     */
    private String getJobLogPath(ExecutionJob executionJob) {
        String baseLog = ((JobLogDiscriminator) ((SiftingAppender) ((LoggerContext) LoggerFactory.getILoggerFactory())
                .getLogger("ROOT")
                .getAppender("JOB_LOG_FILE"))
                .getDiscriminator()).getLogBase();
        if (baseLog.startsWith(CommonConstants.SINGLE_SLASH)){
            return baseLog + CommonConstants.SINGLE_SLASH +
                    executionJob.getEngineType() + CommonConstants.SINGLE_SLASH  +
                    executionJob.getJobInstanceId() + ".log";
        }
        return System.getProperty("user.dir") + CommonConstants.SINGLE_SLASH +
                baseLog + CommonConstants.SINGLE_SLASH +
                executionJob.getEngineType() + CommonConstants.SINGLE_SLASH  +
                executionJob.getJobInstanceId() + ".log";
    }

}
