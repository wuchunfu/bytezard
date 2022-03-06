package io.datavines.coordinator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties("coordinator")
public class CoordinatorConfiguration {

    private String deployDir = "/datavines";

    private String leaderElectionDir = "/datavines/leader_election";

    private String hostSelectStrategy = "round_robin";

    private Integer listenPort = 5677;

    private Integer servletPort = 5679;

    private String recoveryMode = "ZOOKEEPER";

    private String zookeeperQuorum = "localhost:2181";

    public String getDeployDir() {
        return deployDir;
    }

    public void setDeployDir(String deployDir) {
        this.deployDir = deployDir;
    }

    public String getLeaderElectionDir() {
        return leaderElectionDir;
    }

    public void setLeaderElectionDir(String leaderElectionDir) {
        this.leaderElectionDir = leaderElectionDir;
    }

    public String getHostSelectStrategy() {
        return hostSelectStrategy;
    }

    public void setHostSelectStrategy(String hostSelectStrategy) {
        this.hostSelectStrategy = hostSelectStrategy;
    }

    public Integer getListenPort() {
        return listenPort;
    }

    public void setListenPort(Integer listenPort) {
        this.listenPort = listenPort;
    }

    public Integer getServletPort() {
        return servletPort;
    }

    public void setServletPort(Integer servletPort) {
        this.servletPort = servletPort;
    }

    public String getRecoveryMode() {
        return recoveryMode;
    }

    public void setRecoveryMode(String recoveryMode) {
        this.recoveryMode = recoveryMode;
    }

    public String getZookeeperQuorum() {
        return zookeeperQuorum;
    }

    public void setZookeeperQuorum(String zookeeperQuorum) {
        this.zookeeperQuorum = zookeeperQuorum;
    }


}
