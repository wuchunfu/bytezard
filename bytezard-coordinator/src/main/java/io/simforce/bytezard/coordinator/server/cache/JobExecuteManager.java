package io.simforce.bytezard.coordinator.server.cache;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.simforce.bytezard.common.entity.TaskRequest;
import io.simforce.bytezard.common.utils.JSONUtils;
import io.simforce.bytezard.common.utils.Stopper;
import io.simforce.bytezard.common.utils.ThreadUtils;
import io.simforce.bytezard.coordinator.repository.entity.Task;
import io.simforce.bytezard.common.enums.ExecutionStatus;
import io.simforce.bytezard.coordinator.exception.ExecuteJobException;
import io.simforce.bytezard.coordinator.server.channel.ClientChannel;
import io.simforce.bytezard.coordinator.server.channel.ClientChannelManager;
import io.simforce.bytezard.coordinator.config.CoordinatorConfiguration;
import io.simforce.bytezard.coordinator.server.context.ExecutionContext;
import io.simforce.bytezard.coordinator.server.dispatch.ExecuteResult;
import io.simforce.bytezard.coordinator.server.dispatch.IJobDispatcher;
import io.simforce.bytezard.coordinator.server.dispatch.JobDispatcherImpl;
import io.simforce.bytezard.coordinator.server.processor.JobResponseContext;
import io.simforce.bytezard.coordinator.repository.service.impl.JobExternalService;
import io.simforce.bytezard.coordinator.utils.SpringApplicationContext;
import io.simforce.bytezard.remote.command.CommandCode;
import io.simforce.bytezard.remote.command.JobExecuteRequestCommand;
import io.simforce.bytezard.remote.command.JobExecuteResponseCommand;
import io.simforce.bytezard.remote.command.JobKillRequestCommand;
import io.simforce.bytezard.remote.command.RequestClientType;
import io.simforce.bytezard.remote.utils.ChannelUtils;
import io.simforce.bytezard.remote.utils.Host;
import io.simforce.bytezard.remote.utils.NamedThreadFactory;

public class JobExecuteManager {

    private final Logger logger = LoggerFactory.getLogger(JobExecuteManager.class);

    private final LinkedBlockingQueue<CommandContext> jobQueue = new LinkedBlockingQueue<>();

    private final ConcurrentHashMap<Long, TaskRequest> unFinishedJobMap = new ConcurrentHashMap<>();
    
    private final LinkedBlockingQueue<JobResponseContext> responseQueue = new LinkedBlockingQueue<>();
    
    private final ConcurrentHashMap<Host,Set<Long>> executor2Jobs = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Long,String> taskId2ClientIp = new ConcurrentHashMap<>();

    private final IJobDispatcher<ExecuteResult> jobDispatcher;

    private final ExecutorService executorService;

    private final ClientChannelManager clientChannelManager;

    private final JobExternalService jobExternalService;

    private final HashedWheelTimer wheelTimer =
            new HashedWheelTimer(new NamedThreadFactory("Job-Execute-Timeout"),1,TimeUnit.SECONDS);

    public JobExecuteManager(CoordinatorConfiguration configuration){

        executorService = Executors.newFixedThreadPool(5,new NamedThreadFactory("Coordinator-Job-Manager"));

        jobDispatcher = new JobDispatcherImpl(configuration);

        clientChannelManager = ClientChannelManager.getInstance();

        jobExternalService = SpringApplicationContext.getBean(JobExternalService.class);
    }

    public void start(){
        JobSender jobSender = new JobSender();
        executorService.submit(jobSender);
        logger.info("job sender start");

        JobResponseOperator responseOperator = new JobResponseOperator();
        executorService.submit(responseOperator);
        logger.info("job response operator start");
    }

    public boolean addExecuteCommand(TaskRequest taskRequest){
        logger.info("put into wait to send {}", JSONUtils.toJsonString(taskRequest));
        unFinishedJobMap.put(taskRequest.getTaskId(), taskRequest);
        CommandContext commandContext = new CommandContext();
        commandContext.setCommandCode(CommandCode.JOB_EXECUTE_REQUEST);
        commandContext.setTaskId(taskRequest.getTaskId());
        commandContext.setTaskRequest(taskRequest);
        jobQueue.offer(commandContext);

        return true;
        //记录该任务从哪个client传过来，会将结果发送回该client
    }

    public void addKillCommand(Long taskId){
        CommandContext commandContext = new CommandContext();
        commandContext.setCommandCode(CommandCode.JOB_KILL_REQUEST);
        commandContext.setTaskId(taskId);
        jobQueue.offer(commandContext);
    }

    public void putTaskId2ClientIpMap(Long taskId,String ip){
        this.taskId2ClientIp.put(taskId,ip);
    }

    public void putUnStartedJobs(List<TaskRequest> taskRequests){
        if (taskRequests != null) {
            for (TaskRequest taskRequest : taskRequests) {
                addExecuteCommand(taskRequest);
            }
        }
    }

    public void putUnFinishedJobs(List<TaskRequest> taskRequests) {
        if (taskRequests != null) {
            for (TaskRequest taskRequest : taskRequests) {
                unFinishedJobMap.put(taskRequest.getTaskId(), taskRequest);
            }
        }
    }

    public TaskRequest getExecutionJob(Long taskId){
        return unFinishedJobMap.get(taskId);
    }

    class JobSender implements Runnable {

        @Override
        public void run() {
            while(Stopper.isRunning()) {
                try {
                    CommandContext commandContext = jobQueue.take();
                    Long taskId = commandContext.getTaskId();
                    switch(commandContext.getCommandCode()){
                        case JOB_EXECUTE_REQUEST:
                            sendExecuteCommand(commandContext, taskId);
                            break;
                        case JOB_KILL_REQUEST:
                            sendKillCommand(taskId);
                            break;
                        default:
                            break;
                    }

                } catch(Exception e) {
                    logger.error("dispatcher job error",e);
                    ThreadUtils.sleep(2000);
                }
            }
        }
    }

    private void sendExecuteCommand(CommandContext commandContext, Long taskId) {
        TaskRequest job = unFinishedJobMap.get(taskId);
        JobExecuteRequestCommand jobExecuteRequestCommand = new JobExecuteRequestCommand(JSONUtils.toJsonString(job));
        ExecutionContext executionContext = new ExecutionContext(jobExecuteRequestCommand.convert2Command(), RequestClientType.EXECUTOR);

        logger.info("get the executionJob:{} to send",executionContext);
        try {
            ExecuteResult result = jobDispatcher.execute(executionContext);
            if (result != null && result.getResult()) {
                logger.info("JobSender:"+taskId+"_"+JSONUtils.toJsonString(unFinishedJobMap.get(taskId)));
                putJobInExecutor2JobMap(taskId, result);
                wheelTimer.newTimeout(new JobTimeoutTimerTask(taskId,job.getRetryTimes()),job.getTimeout(),TimeUnit.SECONDS);
            }
        } catch(Exception e) {
            jobQueue.offer(commandContext);
            logger.error("dispatcher job error",e);
            ThreadUtils.sleep(2000);
        }
    }

    private void sendKillCommand(Long taskId) {
        JobKillRequestCommand jobKillRequestCommand = new JobKillRequestCommand(taskId);
        ExecutionContext executionContext2 = new ExecutionContext(jobKillRequestCommand.convert2Command(),RequestClientType.EXECUTOR);

        logger.info("get the executionJob:{} to send",executionContext2);
        try{
            TaskRequest job = unFinishedJobMap.get(taskId);
            if (job == null) {
                throw new ExecuteJobException("job is not exist,can not kill the job");
            }

            String address = job.getExecuteHost();
            logger.info("job execute host {}",address);
            if (StringUtils.isEmpty(address)) {
                throw new ExecuteJobException("job execute host is empty,can not kill the job");
            }

            Host host = Host.of(address);
            ClientChannel clientChannel = clientChannelManager.getChannel(host,RequestClientType.EXECUTOR);
            if (clientChannel == null) {
                throw new ExecuteJobException("job execute host is null,can not kill the job");
            }

            ExecuteResult result = jobDispatcher.execute(executionContext2,clientChannel);
            if (result != null && result.getResult()) {
                logger.info("JobSender:"+taskId+"_"+JSONUtils.toJsonString(unFinishedJobMap.get(taskId)));
                //将taskId从缓存中移除掉
                putJobInExecutor2JobMap(taskId, result);
            }
        } catch(Exception e) {
            logger.error("dispatcher job error",e);
            ThreadUtils.sleep(2000);
        }
    }

    private void putJobInExecutor2JobMap(Long taskId, ExecuteResult result) {
        Host host = ChannelUtils.toAddress(result.getChannel());

        Set<Long> jobs = executor2Jobs.get(host);
        if (jobs == null) {
            jobs = new HashSet<>();
        }
        jobs.add(taskId);

        executor2Jobs.put(host,jobs);
    }

    /**
     * operate the job response
     */
    class JobResponseOperator implements Runnable {

        @Override
        public void run() {
            while (Stopper.isRunning()) {
                try {
                    JobResponseContext jobResponse = responseQueue.take();

                    TaskRequest job = jobResponse.getTaskRequest();
                    unFinishedJobMap.put(job.getTaskId(),job);
                    logger.info(JSONUtils.toJsonString(job));

                    if (ExecutionStatus.of(job.getStatus()).typeIsSuccess()) {
                        unFinishedJobMap.remove(job.getTaskId());

                        jobExternalService.updateTaskStatus(job.getTaskId(), job.getStatus());

                        String clientIp = taskId2ClientIp.get(job.getTaskId());
                        if (StringUtils.isNotEmpty(clientIp)) {
                            JobExecuteResponseCommand jobExecuteResponseCommand =
                                    new JobExecuteResponseCommand();
                            jobExecuteResponseCommand.setApplicationIds(job.getApplicationId());
                            jobExecuteResponseCommand.setTaskId(job.getTaskId());
                            jobExecuteResponseCommand.setProcessId(job.getProcessId());
                            jobExecuteResponseCommand.setStatus(job.getStatus());
                            job.setEndTime(job.getEndTime());
                            ClientChannelManager
                                    .getInstance()
                                    .getClientByIp(clientIp)
                                    .getChannel()
                                    .writeAndFlush(jobExecuteResponseCommand.convert2Command());
                        }

                    } else if (ExecutionStatus.of(job.getStatus()).typeIsFailure()) {
//                        TaskRequest oldJob = unFinishedJobMap.get(job.getTaskId());
                        Task task = jobExternalService.getTaskById(job.getTaskId());
                        logger.info("retry task: "+JSONUtils.toJsonString(task));

                        int retryNums = task.getRetryTimes();
                        if (task.getRetryTimes() > 0) {
                            CommandContext commandContext = new CommandContext();
                            commandContext.setTaskRequest(jobExternalService.buildTaskRequest(task));
                            commandContext.setTaskId(job.getTaskId());
                            commandContext.setCommandCode(CommandCode.JOB_EXECUTE_REQUEST);
                            jobQueue.offer(commandContext);
                            jobExternalService.updateTaskRetryTimes(job.getTaskId(), retryNums - 1);
                        } else {
                            unFinishedJobMap.remove(job.getTaskId());
                        }
                    } else if(ExecutionStatus.of(job.getStatus()).typeIsCancel()) {
                        unFinishedJobMap.remove(job.getTaskId());
                    } else if(ExecutionStatus.of(job.getStatus()).typeIsRunning()) {
                        // do nothing
                    }

                } catch(Exception e) {
                    logger.info("operate job response error {0}",e);
                }
            }
        }
    }

    /**
     * 将来自executor的回复放进队列
     * @param jobResponse
     */
    public void putResponse(JobResponseContext jobResponse){
        responseQueue.offer(jobResponse);
    }

    public void reassignJob(Host host){
        //重试3次，如果一直找不到可用的executor，那么就直接将coordinator的状态切换不可用
        logger.info("start reassign job");

        Set<Long> taskIds = executor2Jobs.get(host);
        if(CollectionUtils.isEmpty(taskIds)){
            logger.info("there's no job need to reassign");
            return;
        }

        int retryNums = 10;
        do {
            if (clientChannelManager.isExecutorChannelListEmpty()) {
                retryNums--;
                ThreadUtils.sleep(1000*(10-retryNums));
                logger.info("retry get executor {}",retryNums);
            } else {
                for (Long taskId:taskIds) {
                    if (unFinishedJobMap.get(taskId) != null) {
                        TaskRequest job = unFinishedJobMap.get(taskId);

                        //发送检查YARN_APPLICATION是否还在运行当中，如果仍然在运行，那么构建检查任务发送

                        CommandContext commandContext = new CommandContext();
                        commandContext.setCommandCode(CommandCode.JOB_EXECUTE_REQUEST);
                        commandContext.setTaskId(taskId);
                        commandContext.setTaskRequest(unFinishedJobMap.get(taskId));
                        jobQueue.offer(commandContext);
                    }
                }

                break;
            }
        } while(Stopper.isRunning() && retryNums>0);
    }

    class JobTimeoutTimerTask implements TimerTask {

        private final long taskId;
        private final int retryTimes;

        public JobTimeoutTimerTask(long taskId,int retryTimes){
            this.taskId = taskId;
            this.retryTimes = retryTimes;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            TaskRequest job = unFinishedJobMap.get(this.taskId);
            if (job == null) {
                logger.info("job {} is finished, do nothing...",taskId);
                return;
            }

            if (this.retryTimes != job.getRetryTimes()) {
                logger.info("job {} is finished, do nothing...",taskId);
                return;
            }

            logger.info("job is timeout,do kill");
            //这个地方可以根据超时策略进行处理
            sendKillCommand(this.taskId);
        }
    }

    public void close(){

    }

}
