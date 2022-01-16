package io.simforce.bytezard.common.entity;

import java.util.Date;

/**
 * @author zixi0825
 */
public class ExecutionJob {

    private long id;
    /**
     * 实例ID
     */
    private long jobInstanceId;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 任务的JSON 字符串
     */
    private String jobJson;

    /**
     * 执行路径
     */
    private String executePath;

    /**
     * 日志路径
     */
    private String logPath;

    /**
     * 结果路径
     */
    private String resultPath;

    /**
     * 执行任务的主机地址
     */
    private String executeHost;

    /**
     * 任务的唯一ID
     */
    private String jobUniqueId;

    /**
     * 进程ID
     */
    private int processId;

    /**
     * 运行在其他平台的application id
     */
    private String applicationId;

    /**
     * 任务的参数
     */
    private String jobParameters;

    private String bytezardConfiguration;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 环境文件
     */
    private String envFile;

    /**
     * 提交时间
     */
    private Date submitTime;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 运行队列
     */
    private String queue;

    /**
     * 任务状态
     */
    private int status;

    /**
     * 执行器组
     */
    private String executorGroup;

    /**
     * 超时时间
     */
    private int timeout;

    /**
     * 超时策略
     */
    private int timeoutStrategy;

    /**
     * 重试次数
     */
    private int retryNums;

    /**
     * 资源路径
     */
    private String resources;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date modifyTime;

    public ExecutionJob(){}

    public ExecutionJob(long jobInstanceId,
                        String jobName,
                        String jobJson,
                        String executePath,
                        String logPath,
                        String resultPath,
                        String executeHost,
                        String jobUniqueId,
                        String jobParameters,
                        String bytezardConfiguration,
                        String tenantCode,
                        String envFile,
                        Date submitTime,
                        Date startTime,
                        Date endTime,
                        String queue,
                        int status,
                        String executorGroup,
                        int timeout,
                        int timeoutStrategy,
                        int retryNums,
                        String resources,
                        String applicationId,
                        int processId,
                        Date createTime,
                        Date modifyTime) {
        this.jobInstanceId = jobInstanceId;
        this.jobName = jobName;
        this.jobJson = jobJson;
        this.executePath = executePath;
        this.logPath = logPath;
        this.resultPath = resultPath;
        this.executeHost = executeHost;
        this.jobUniqueId = jobUniqueId;
        this.jobParameters = jobParameters;
        this.bytezardConfiguration = bytezardConfiguration;
        this.tenantCode = tenantCode;
        this.envFile = envFile;
        this.submitTime = submitTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.queue = queue;
        this.status = status;
        this.executorGroup = executorGroup;
        this.timeout = timeout;
        this.timeoutStrategy = timeoutStrategy;
        this.retryNums = retryNums;
        this.resources = resources;
        this.applicationId = applicationId;
        this.processId = processId;
        this.createTime = createTime;
        this.modifyTime = modifyTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getJobInstanceId() {
        return jobInstanceId;
    }

    public void setJobInstanceId(long jobInstanceId) {
        this.jobInstanceId = jobInstanceId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobJson() {
        return jobJson;
    }

    public void setJobJson(String jobJson) {
        this.jobJson = jobJson;
    }

    public String getExecutePath() {
        return executePath;
    }

    public void setExecutePath(String executePath) {
        this.executePath = executePath;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getResultPath() {
        return resultPath;
    }

    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }

    public String getExecuteHost() {
        return executeHost;
    }

    public void setExecuteHost(String executeHost) {
        this.executeHost = executeHost;
    }

    public String getJobUniqueId() {
        return jobUniqueId;
    }

    public void setJobUniqueId(String jobUniqueId) {
        this.jobUniqueId = jobUniqueId;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getJobParameters() {
        return jobParameters;
    }

    public void setJobParameters(String jobParameters) {
        this.jobParameters = jobParameters;
    }

    public String getBytezardConfiguration() {
        return bytezardConfiguration;
    }

    public void setBytezardConfiguration(String bytezardConfiguration) {
        this.bytezardConfiguration = bytezardConfiguration;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getEnvFile() {
        return envFile;
    }

    public void setEnvFile(String envFile) {
        this.envFile = envFile;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getExecutorGroup() {
        return executorGroup;
    }

    public void setExecutorGroup(String executorGroup) {
        this.executorGroup = executorGroup;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeoutStrategy() {
        return timeoutStrategy;
    }

    public void setTimeoutStrategy(int timeoutStrategy) {
        this.timeoutStrategy = timeoutStrategy;
    }

    public int getRetryNums() {
        return retryNums;
    }

    public void setRetryNums(int retryNums) {
        this.retryNums = retryNums;
    }

    public String getResources() {
        return resources;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

}
