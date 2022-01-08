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

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.simforce.bytezard.common.entity.ExecutionJob;
import io.simforce.bytezard.common.utils.Stopper;
import io.simforce.bytezard.common.utils.ThreadUtils;
import io.simforce.bytezard.coordinator.repository.entity.JobInstance;
import io.simforce.bytezard.coordinator.eunms.ExecutionStatus;
import io.simforce.bytezard.coordinator.exception.ExecuteJobException;
import io.simforce.bytezard.coordinator.repository.module.BytezardCoordinatorInjector;
import io.simforce.bytezard.coordinator.server.channel.ClientChannel;
import io.simforce.bytezard.coordinator.server.channel.ClientChannelManager;
import io.simforce.bytezard.coordinator.config.CoordinatorConfiguration;
import io.simforce.bytezard.coordinator.server.context.ExecutionContext;
import io.simforce.bytezard.coordinator.server.dispatch.ExecuteResult;
import io.simforce.bytezard.coordinator.server.dispatch.IJobDispatcher;
import io.simforce.bytezard.coordinator.server.dispatch.JobDispatcherImpl;
import io.simforce.bytezard.coordinator.server.processor.JobResponseContext;
import io.simforce.bytezard.coordinator.server.recovery.PersistenceEngine;
import io.simforce.bytezard.coordinator.repository.service.impl.JobExternalService;
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

    private final ConcurrentHashMap<Long, ExecutionJob> unFinishedJobMap = new ConcurrentHashMap<>();
    
    private final LinkedBlockingQueue<JobResponseContext> responseQueue = new LinkedBlockingQueue<>();
    
    private final ConcurrentHashMap<Host,Set<Long>> executor2Jobs = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Long,String> jobInstanceId2ClientIp = new ConcurrentHashMap<>();

    private final IJobDispatcher<ExecuteResult> jobDispatcher;

    private final ExecutorService executorService;

    private final PersistenceEngine persistenceEngine;

    private final ClientChannelManager clientChannelManager;

    private final JobExternalService jobExternalService;

    private final HashedWheelTimer wheelTimer =
            new HashedWheelTimer(new NamedThreadFactory("Job-Execute-Timeout"),1,TimeUnit.SECONDS);

    public JobExecuteManager(CoordinatorConfiguration configuration,
                             PersistenceEngine persistenceEngine){

        this.persistenceEngine = persistenceEngine;

        executorService = Executors.newFixedThreadPool(5,new NamedThreadFactory("Coordinator-Job-Manager"));

        jobDispatcher = new JobDispatcherImpl(configuration);

        clientChannelManager = ClientChannelManager.getInstance();

        jobExternalService = BytezardCoordinatorInjector
                .getInjector()
                .getInstance(JobExternalService.class);
    }

    public void start(){
        JobSender jobSender = new JobSender();
        executorService.submit(jobSender);
        logger.info("job sender start");

        JobResponseOperator responseOperator = new JobResponseOperator();
        executorService.submit(responseOperator);
        logger.info("job response operator start");
    }

    public void addKillCommand(Long jobInstanceId){
        CommandContext commandContext = new CommandContext();
        commandContext.setCommandCode(CommandCode.JOB_KILL_REQUEST);
        commandContext.setJobInstanceId(jobInstanceId);
        jobQueue.offer(commandContext);
    }

    public boolean addExecuteCommand(ExecutionJob executionJob){
        logger.info("put into wait to send {}", JSONUtils.toJSONString(executionJob));
        unFinishedJobMap.put(executionJob.getJobInstanceId(), executionJob);
        CommandContext commandContext = new CommandContext();
        commandContext.setCommandCode(CommandCode.JOB_EXECUTE_REQUEST);
        commandContext.setJobInstanceId(executionJob.getJobInstanceId());
        commandContext.setExecutionJob(executionJob);
        jobQueue.offer(commandContext);

        return true;
        //记录该任务从哪个client传过来，会将结果发送回该client
    }

    public void putJobInstanceId2ClientIpMap(Long jobInstanceId,String ip){
        this.jobInstanceId2ClientIp.put(jobInstanceId,ip);
    }

    public void putUnStartedJobs(List<ExecutionJob> executionJobs){
        if(executionJobs != null){
            for(ExecutionJob executionJob : executionJobs){
                addExecuteCommand(executionJob);
            }
        }
    }

    public void putUnFinishedJobs(List<ExecutionJob> executionJobs){
        if(executionJobs != null){
            for(ExecutionJob executionJob : executionJobs){
                unFinishedJobMap.put(executionJob.getJobInstanceId(), executionJob);
            }
        }
    }

    public PersistenceEngine getPersistenceEngine() {
        return persistenceEngine;
    }

    public ExecutionJob getExecutionJob(Long jobInstanceId){
        return unFinishedJobMap.get(jobInstanceId);
    }

    class JobSender implements Runnable {

        @Override
        public void run() {
            while(Stopper.isRunning()) {
                try {
                    CommandContext commandContext = jobQueue.take();
                    Long jobInstanceId = commandContext.getJobInstanceId();
                    switch(commandContext.getCommandCode()){
                        case JOB_EXECUTE_REQUEST:
                            sendExecuteCommand(commandContext, jobInstanceId);
                            break;
                        case JOB_KILL_REQUEST:
                            sendKillCommand(jobInstanceId);
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

    private void sendExecuteCommand(CommandContext commandContext, Long jobInstanceId) {
        ExecutionJob job = unFinishedJobMap.get(jobInstanceId);
        JobExecuteRequestCommand jobExecuteRequestCommand = new JobExecuteRequestCommand(JSON.toJSONString(job));
        ExecutionContext executionContext = new ExecutionContext(jobExecuteRequestCommand.convert2Command(), RequestClientType.EXECUTOR);

        logger.info("get the executionJob:{} to send",executionContext);
        try {
            ExecuteResult result = jobDispatcher.execute(executionContext);
            if (result != null && result.getResult()) {
                logger.info("JobSender:"+jobInstanceId+"_"+JSONUtils.toJSONString(unFinishedJobMap.get(jobInstanceId)));
                putJobInExecutor2JobMap(jobInstanceId, result);
                wheelTimer.newTimeout(new JobTimeoutTimerTask(jobInstanceId,job.getRetryNums()),job.getTimeout(),TimeUnit.SECONDS);
            }
        } catch(Exception e) {
            jobQueue.offer(commandContext);
            logger.error("dispatcher job error",e);
            ThreadUtils.sleep(2000);
        }
    }

    private void sendKillCommand(Long jobInstanceId) {
        JobKillRequestCommand jobKillRequestCommand = new JobKillRequestCommand(jobInstanceId);
        ExecutionContext executionContext2 = new ExecutionContext(jobKillRequestCommand.convert2Command(),RequestClientType.EXECUTOR);

        logger.info("get the executionJob:{} to send",executionContext2);
        try{
            ExecutionJob job = unFinishedJobMap.get(jobInstanceId);
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
                logger.info("JobSender:"+jobInstanceId+"_"+JSONUtils.toJSONString(unFinishedJobMap.get(jobInstanceId)));
                //将jobInstanceId从缓存中移除掉
                putJobInExecutor2JobMap(jobInstanceId, result);
            }
        } catch(Exception e) {
            logger.error("dispatcher job error",e);
            ThreadUtils.sleep(2000);
        }
    }

    private void putJobInExecutor2JobMap(Long jobInstanceId, ExecuteResult result) {
        Host host = ChannelUtils.toAddress(result.getChannel());

        Set<Long> jobs = executor2Jobs.get(host);
        if (jobs == null) {
            jobs = new HashSet<>();
        }
        jobs.add(jobInstanceId);

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

                    ExecutionJob job = jobResponse.getExecutionJob();
                    unFinishedJobMap.put(job.getJobInstanceId(),job);
                    logger.info(JSONUtils.toJSONString(job));

                    if(ExecutionStatus.of(job.getStatus()).typeIsSuccess()){
                        persistenceEngine.update("",job);
                        unFinishedJobMap.remove(job.getJobInstanceId());

                        JobInstance jobInstance = jobExternalService.getJobInstanceByExecutionId(job.getJobInstanceId());
                        jobInstance.setStatus(job.getStatus());
                        jobExternalService.updateJobInstance(jobInstance);

                        String clientIp = jobInstanceId2ClientIp.get(job.getJobInstanceId());
                        if(StringUtils.isNotEmpty(clientIp)){
                            JobExecuteResponseCommand jobExecuteResponseCommand =
                                    new JobExecuteResponseCommand();
                            jobExecuteResponseCommand.setApplicationIds(job.getApplicationId());
                            jobExecuteResponseCommand.setJobInstanceId(job.getJobInstanceId());
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
                        ExecutionJob oldJob = unFinishedJobMap.get(job.getJobInstanceId());
                        logger.info("retry job: "+JSONUtils.toJSONString(oldJob));
                        int retryNums = oldJob.getRetryNums();
                        if (oldJob.getRetryNums() > 0) {
                            CommandContext commandContext = new CommandContext();
                            commandContext.setExecutionJob(oldJob);
                            commandContext.setJobInstanceId(oldJob.getJobInstanceId());
                            commandContext.setCommandCode(CommandCode.JOB_EXECUTE_REQUEST);
                            jobQueue.offer(commandContext);
                            oldJob.setRetryNums(retryNums - 1);
                        } else {
                            persistenceEngine.update("",job);
                            unFinishedJobMap.remove(job.getJobInstanceId());
                        }
                    } else if(ExecutionStatus.of(job.getStatus()).typeIsCancel()) {
                        persistenceEngine.update("",job);
                        unFinishedJobMap.remove(job.getJobInstanceId());
                    } else if(ExecutionStatus.of(job.getStatus()).typeIsRunning()) {
                        persistenceEngine.update("",job);
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
        //重试3次，如果一直找不到可用的executor，那么就直接将master的状态切换不可用
        logger.info("start reassign job");

        Set<Long> jobInstanceIds = executor2Jobs.get(host);
        if(CollectionUtils.isEmpty(jobInstanceIds)){
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
                for (Long jobInstanceId:jobInstanceIds) {
                    if (unFinishedJobMap.get(jobInstanceId) != null) {
                        ExecutionJob job = unFinishedJobMap.get(jobInstanceId);

                        //发送检查YARN_APPLICATION是否还在运行当中，如果仍然在运行，那么构建检查任务发送

                        CommandContext commandContext = new CommandContext();
                        commandContext.setCommandCode(CommandCode.JOB_EXECUTE_REQUEST);
                        commandContext.setJobInstanceId(jobInstanceId);
                        commandContext.setExecutionJob(unFinishedJobMap.get(jobInstanceId));
                        jobQueue.offer(commandContext);
                    }
                }
                break;
            }
        } while(Stopper.isRunning() && retryNums>0);
    }

    class JobTimeoutTimerTask implements TimerTask {

        private final long jobInstanceId;
        private final int retryTimes;

        public JobTimeoutTimerTask(long jobInstanceId,int retryTimes){
            this.jobInstanceId = jobInstanceId;
            this.retryTimes = retryTimes;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            ExecutionJob job = unFinishedJobMap.get(this.jobInstanceId);
            if (job == null) {
                logger.info("job {} is finished, do nothing...",jobInstanceId);
                return;
            }

            if (this.retryTimes != job.getRetryNums()) {
                logger.info("job {} is finished, do nothing...",jobInstanceId);
                return;
            }

            logger.info("job is timeout,do kill");
            //这个地方可以根据超时策略进行处理
            sendKillCommand(this.jobInstanceId);
        }
    }

    public void close(){

    }

}
