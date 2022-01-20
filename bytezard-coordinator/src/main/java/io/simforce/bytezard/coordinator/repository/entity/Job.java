package io.simforce.bytezard.coordinator.repository.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Job implements Serializable {

    private static final long serialVersionUID = -1L;

    private Long id;

    private String name;

    /**
     * local/yarn/k8s
     */
    private String executePlatformType;

    /**
     * {@link io.simforce.bytezard.common.entity.JobParameter}
     */
    private String parameter;

    private Long projectId;

    private Integer retryTimes;

    private Integer retryInterval;

    private Integer timeout;

    private Integer timeoutStrategy;

    private String tenantCode;

    private String resources;

    private Long version;

    private Long createBy;

    private LocalDateTime createTime;

    private Long updateBy;

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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
