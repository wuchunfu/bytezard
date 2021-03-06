package io.datavines.engine.executor.core.executor;

import static io.datavines.engine.api.EngineConstants.EXIT_CODE_FAILURE;
import static io.datavines.engine.api.EngineConstants.EXIT_CODE_SUCCESS;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import io.datavines.common.CommonConstants;
import io.datavines.common.config.Configurations;
import io.datavines.common.config.CoreConfig;
import io.datavines.common.entity.TaskRequest;
import io.datavines.common.entity.ProcessResult;
import io.datavines.common.utils.LoggerUtils;
import io.datavines.common.utils.ProcessUtils;
import io.datavines.common.utils.ThreadUtils;
import io.datavines.common.utils.YarnUtils;
import io.datavines.engine.api.EngineConstants;

public abstract class BaseCommandProcess {

    /**
     * rules for extracting application ID
     */
    protected static final Pattern APPLICATION_REGEX = Pattern.compile(EngineConstants.APPLICATION_REGEX);

    /**
     *  process
     */
    private Process process;

    /**
     *  log handler
     */
    protected Consumer<List<String>> logHandler;

    /**
     * execution job
     */
    protected TaskRequest taskRequest;

    /**
     *  logger
     */
    protected Logger logger;

    private final Configurations configurations;

    /**
     *  log list
     */
    protected final List<String> logBuffer;

    public BaseCommandProcess(Consumer<List<String>> logHandler,
                              Logger logger,
                              TaskRequest taskRequest,
                              Properties properties){
        this.logHandler = logHandler;
        this.taskRequest = taskRequest;
        this.logger = logger;
        this.logBuffer = Collections.synchronizedList(new ArrayList<>());
        this.configurations = new Configurations(properties);
    }

    public ProcessResult run(String executeCommand){

        ProcessResult result = new ProcessResult();
        int exitStatusCode = -1;
        try{
            if (StringUtils.isEmpty(executeCommand)) {
                return result;
            }

            //?????????????????????????????????
            String commandFilePath = buildCommandFilePath();
            //?????????????????????
            createCommandFileIfNotExists(executeCommand,commandFilePath);
            //??????Process??????????????????????????????
            buildProcess(commandFilePath);
            //????????????????????????
            parseProcessOutput(process);
            //??????process????????????pid
            int pid = getProcessId(process);

            result.setProcessId(pid);
            //??????????????????
            boolean status = process.waitFor(getRemainTime(),TimeUnit.SECONDS);

            if(status){
                exitStatusCode = process.exitValue();

                // set appIds
                List<String> appIds = getApplicationId(taskRequest.getLogPath());
                result.setApplicationId(String.join(CommonConstants.COMMA, appIds));

                // SHELL job state
                result.setExitStatusCode(process.exitValue());

                // if yarn job , yarn state is final state
                if (process.exitValue() == 0){
                    result.setExitStatusCode(YarnUtils.isSuccessOfYarnState(appIds) ? EXIT_CODE_SUCCESS : EXIT_CODE_FAILURE);
                }

                logger.info("process has exited, work dir:{}, pid:{} ,exitStatusCode:{}", taskRequest.getExecuteFilePath(), pid,exitStatusCode);
            } else {
                logger.warn("process timeout, work dir:{}, pid:{}", taskRequest.getExecuteFilePath(), pid);
            }

        } catch (InterruptedException e) {
            logger.error("interrupt exception:{0}, job may be cancelled or killed", e);
            throw new RuntimeException("interrupt exception. exitCode is :  " + exitStatusCode);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("process error . exitCode is :  " + exitStatusCode);
        }

        return result;
    }

    /**
     * build process to execute
     * @param commandFile
     * @throws IOException
     */
    private void buildProcess(String commandFile) throws IOException{

        //init process builder
        ProcessBuilder processBuilder = new ProcessBuilder();
        // setting up a working directory
        processBuilder.directory(new File(taskRequest.getExecuteFilePath()));
        // merge error information to standard output stream
        processBuilder.redirectErrorStream(true);
        // setting up user to run commands
        List<String> command = new LinkedList<>();
        command.add("sudo");
        command.add("-u");
        command.add(taskRequest.getTenantCode());
        command.add(commandInterpreter());
        command.addAll(commandOptions());
        command.add(commandFile);
        processBuilder.command(command);

        process = processBuilder.start();

        // print command
        printCommand(processBuilder);

    }

    /**
     * find app id
     * @param line line
     * @return appid
     */
    private String findAppId(String line) {
        Matcher matcher = APPLICATION_REGEX.matcher(line);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * get app links
     *
     * @param logPath log path
     * @return app id list
     */
    private List<String> getApplicationId(String logPath) {
        List<String> logs = convertFile2List(logPath);
        return getApplicationId(logs);
    }

    private List<String> getApplicationId(List<String> logs) {
        List<String> appIds = new ArrayList<>();
        for (String log : logs) {
            String appId = findAppId(log);
            if (StringUtils.isNotEmpty(appId) && !appIds.contains(appId)) {
                logger.info("find app id: {}", appId);
                appIds.add(appId);
            }
        }
        return appIds;
    }

    /**
     * convert file to list
     * @param filename file name
     * @return line list
     */
    private List<String> convertFile2List(String filename) {
        List<String> lineList = new ArrayList<>(100);
        File file=new File(filename);

        if (!file.exists()){
            return lineList;
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8));
            String line = null;
            while ((line = br.readLine()) != null) {
                lineList.add(line);
            }
        } catch (Exception e) {
            logger.error(String.format("read file: %s failed : ",filename),e);
        } finally {
            if(br != null){
                try {
                    br.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(),e);
                }
            }

        }
        return lineList;
    }

    /**
     * get the process id
     * @param process process
     * @return processId
     */
    private int getProcessId(Process process){
        int processId = 0;

        try{
            Field field = process.getClass().getDeclaredField(EngineConstants.PID);
            field.setAccessible(true);
            processId = field.getInt(process);
        }catch (Throwable e){
            logger.error(e.getMessage(),e);
        }

        return processId;
    }

    /**
     * print command
     * @param processBuilder process builder
     */
    private void printCommand(ProcessBuilder processBuilder) {
        String cmdStr;

        try {
            cmdStr = ProcessUtils.buildCommandStr(processBuilder.command());
            logger.info("job run command:\n{}", cmdStr);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * get remain time???s???
     *
     * @return remain time
     */
    private long getRemainTime() {
        long usedTime = (System.currentTimeMillis() - taskRequest.getStartTime().toInstant(ZoneOffset.of("+8")).toEpochMilli()) / 1000;
        long remainTime = taskRequest.getTimeout() - usedTime;

        if (remainTime < 0) {
            throw new RuntimeException("job execution time out");
        }

        return remainTime;
    }

    public void cancel(){
        if(process == null){
            return;
        }

        int pid = getProcessId(process);

        logger.info("cancel process {}",pid);

        boolean isKilled = softKill(pid);
        if(!isKilled){
            hardKill(pid);
            process.destroy();
            process = null;
        }

        clearLog();

    }

    /**
     * clear
     */
    private void clearLog() {
        if (!logBuffer.isEmpty()) {
            // log handle
            logHandler.accept(logBuffer);
            logBuffer.clear();
        }
    }


    /**
     * soft kill
     * @param processId processId
     * @return boolean
     */
    private boolean softKill(int processId) {
        if (processId != 0 && process.isAlive()) {
            try {
                // sudo -u user command to run command
                String cmd = String.format("sudo kill %d", processId);

                logger.info("soft kill job:{}, process id:{}, cmd:{}", taskRequest.getTaskName(), processId, cmd);

                Runtime.getRuntime().exec(cmd);
            } catch (IOException e) {
                logger.info("kill attempt failed." + e.getMessage(), e);
            }
        }

        return process.isAlive();
    }

    /**
     * hard kill
     * @param processId process id
     */
    private void hardKill(int processId) {
        if (processId != 0 && process.isAlive()) {
            try {
                String cmd = String.format("sudo kill -9 %d", processId);

                logger.info("hard kill job:{}, process id:{}, cmd:{}", taskRequest.getTaskName(), processId, cmd);

                Runtime.getRuntime().exec(cmd);
            } catch (IOException e) {
                logger.error("kill attempt failed." + e.getMessage(), e);
            }
        }
    }

    /**
     * get the standard output of the process
     * @param process process
     */
    private void parseProcessOutput(Process process) {
        String threadLoggerInfoName = String.format(LoggerUtils.JOB_LOGGER_THREAD_NAME + "-%s", taskRequest.getTaskName());
        ExecutorService parseProcessOutputExecutorService = ThreadUtils.newDaemonSingleThreadExecutor(threadLoggerInfoName);
        parseProcessOutputExecutorService.submit(new Runnable(){
            @Override
            public void run() {
                BufferedReader inReader = null;

                try {
                    inReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;

                    long lastFlushTime = System.currentTimeMillis();

                    while ((line = inReader.readLine()) != null) {
                        logBuffer.add(line);
                        lastFlushTime = flush(lastFlushTime);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(),e);
                } finally {
                    clear();
                    close(inReader);
                }
            }
        });
        parseProcessOutputExecutorService.shutdown();
    }

    private long flush(long lastFlushTime) {
        long now = System.currentTimeMillis();

        //when log buffer siz or flush time reach condition , then flush
        if (logBuffer.size() >=
                this.configurations.getInt(CoreConfig.LOG_CACHE_ROW_NUM,CoreConfig.LOG_CACHE_ROW_NUM_DEFAULT_VALUE)
                || now - lastFlushTime > this.configurations.getInt(CoreConfig.LOG_FLUSH_INTERVAL,CoreConfig.LOG_FLUSH_INTERVAL_DEFAULT_VALUE)) {
            lastFlushTime = now;
            logHandler.accept(logBuffer);
            //??????????????????application id??????????????????command ????????????
//            List<String> appIds = getApplicationIds(logBuffer);
//            String applicationIds = String.join(Constants.COMMA, appIds);
//            if(CollectionUtils.isNotEmpty(appIds) && StringUtils.isNoneEmpty(applicationIds)){
//                SpringApplicationContext
//                        .getBean(JobCallbackService.class)
//                        .send(executionJob.getProcessId(),
//                              new JobReportInfoCommand(executionJob.getTaskId(),applicationIds).convert2Command());
//            }

            logBuffer.clear();
        }

        return lastFlushTime;
    }

    /**
     * clear
     */
    private void clear() {
        if (!logBuffer.isEmpty()) {
            logHandler.accept(logBuffer);
            logBuffer.clear();
        }
    }

    /**
     * close buffer reader
     * @param inReader in reader
     */
    private void close(BufferedReader inReader) {
        if (inReader != null) {
            try {
                inReader.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }


    protected List<String> commandOptions() {
        return Collections.emptyList();
    }

    /**
     * command interpreter
     * @return
     */
    protected abstract String commandInterpreter();

    /**
     * build command file path
     * @return
     */
    protected abstract String buildCommandFilePath();

    /**
     * create command file if not exists
     * @param execCommand
     * @param commandFile
     * @throws IOException
     */
    protected abstract void createCommandFileIfNotExists(String execCommand, String commandFile) throws IOException;

}
