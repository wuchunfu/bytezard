package io.datavines.coordinator.server.recovery.elector;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.datavines.common.config.CoreConfig;
import io.datavines.common.elector.LeaderElectable;
import io.datavines.common.elector.LeadershipStatus;
import io.datavines.common.entity.ActiveNodeInfo;
import io.datavines.common.utils.JSONUtils;
import io.datavines.common.utils.OSUtils;
import io.datavines.common.zookeeper.ZooKeeperClient;
import io.datavines.coordinator.config.CoordinatorConfiguration;
import io.datavines.coordinator.server.recovery.MetaDataRecover;
import io.datavines.coordinator.server.recovery.RecoveryState;

public class CoordinatorLeaderElector implements LeaderElectable {

    private final Logger logger  = LoggerFactory.getLogger(CoordinatorLeaderElector.class);

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private LeadershipStatus status = LeadershipStatus.NOT_LEADER;

    private final ZooKeeperClient zooKeeperClient;

    private final CoordinatorConfiguration configuration;

    private final MetaDataRecover metaDataRecover;

    private ActiveNodeInfo activeNodeInfo;

    public CoordinatorLeaderElector(CoordinatorConfiguration configuration,
                                    MetaDataRecover metaDataRecover){
        this.configuration = configuration;
        this.zooKeeperClient = ZooKeeperClient.getInstance();
        this.metaDataRecover = metaDataRecover;
    }

    @Override
    public void electedLeader() {
        logger.info("i am the leader");
        updateLeadershipStatus(true);
    }

    @Override
    public void revokedLeadership() {
        logger.info("i am not the leader");
        countDownLatch.countDown();
        updateLeadershipStatus(false);
    }

    @Override
    public void await() {
        try {
            countDownLatch.await(300000,TimeUnit.MILLISECONDS);
        } catch(Exception e) {
            logger.error("coordinator leader elector count down await error {0}",e);
        }
    }

    private void updateLeadershipStatus(boolean isLeader) {
        if (isLeader && status == LeadershipStatus.NOT_LEADER) {
            status = LeadershipStatus.LEADER;
            if (activeNodeInfo == null) {
                activeNodeInfo =
                    new ActiveNodeInfo(OSUtils.getHost(),
                            this.configuration.getListenPort(),
                            this.configuration.getServletPort());
            }
            zooKeeperClient.
                    persistEphemeral(CoreConfig.COORDINATOR_ACTIVE_DIR, JSONUtils.toJsonString(activeNodeInfo));
            metaDataRecover.startRecoverMeta();
            countDownLatch.countDown();

        } else if(!isLeader && status == LeadershipStatus.LEADER) {
            if(activeNodeInfo == null){
                activeNodeInfo =
                    new ActiveNodeInfo(OSUtils.getHost(),
                            this.configuration.getListenPort(),
                            this.configuration.getServletPort());
            }
            zooKeeperClient.remove(CoreConfig.COORDINATOR_ACTIVE_DIR);
            status = LeadershipStatus.NOT_LEADER;
        }
    }

    @Override
    public boolean isActive(){
        return status == LeadershipStatus.LEADER
                && metaDataRecover.getRecoveryState() == RecoveryState.COMPLETED_RECOVERY;
    }

}
