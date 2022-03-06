package io.datavines.coordinator.repository.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("datavines_task")
public class Task implements Serializable {

    private static final long serialVersionUID = -1L;

    @TableId(type= IdType.ASSIGN_ID)
    private Long id;

    @TableField(value = "name")
    private String name;

    @TableField(value = "job_id")
    private Long jobId;

    @TableField(value = "project_id")
    private Long projectId;

    /**
     * local/yarn/k8s
     */
    @TableField(value = "execute_platform_type")
    private String executePlatformType;

    /**
     * {@link io.datavines.common.entity.JobParameter}
     */
    @TableField(value = "parameter")
    private String parameter;

    @TableField(value = "status")
    private Integer status;

    @TableField(value = "retry_times")
    private Integer retryTimes;

    @TableField(value = "retry_interval")
    private Integer retryInterval;

    @TableField(value = "timeout")
    private Integer timeout;

    @TableField(value = "timeout_strategy")
    private Integer timeoutStrategy;

    @TableField(value = "tenant_code")
    private String tenantCode;

    @TableField(value = "resources")
    private String resources;

    @TableField(value = "execute_host")
    private String executeHost;

    @TableField(value = "application_id")
    private String applicationId;

    @TableField(value = "process_id")
    private String processId;

    @TableField(value = "execute_file_path")
    private String executeFilePath;

    @TableField(value = "log_path")
    private String logPath;

    @TableField(value = "submit_time")
    private LocalDateTime submitTime;

    @TableField(value = "start_time")
    private LocalDateTime startTime;

    @TableField(value = "end_time")
    private LocalDateTime endTime;

    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getExecutePlatformType() {
        return executePlatformType;
    }

    public void setExecutePlatformType(String executePlatformType) {
        this.executePlatformType = executePlatformType;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(Integer retryTimes) {
        this.retryTimes = retryTimes;
    }

    public Integer getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(Integer retryInterval) {
        this.retryInterval = retryInterval;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getTimeoutStrategy() {
        return timeoutStrategy;
    }

    public void setTimeoutStrategy(Integer timeoutStrategy) {
        this.timeoutStrategy = timeoutStrategy;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getResources() {
        return resources;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public String getExecuteHost() {
        return executeHost;
    }

    public void setExecuteHost(String executeHost) {
        this.executeHost = executeHost;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getExecuteFilePath() {
        return executeFilePath;
    }

    public void setExecuteFilePath(String executeFilePath) {
        this.executeFilePath = executeFilePath;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
