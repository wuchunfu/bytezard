package io.simforce.bytezard.coordinator.server.recovery.elector;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.simforce.bytezard.common.elector.LeaderElectionAgent;
import io.simforce.bytezard.common.zookeeper.ZooKeeperClient;
import io.simforce.bytezard.coordinator.config.CoordinatorConfiguration;

/**
 * @author zixi0825
 */
public class ZooKeeperLeaderElectionAgent extends LeaderElectionAgent implements LeaderLatchListener {

    private final Logger logger = LoggerFactory.getLogger(ZooKeeperLeaderElectionAgent.class);

    private CuratorFramework zookeeper;

    private LeaderLatch leaderLatch;

    private final ZooKeeperClient zooKeeperClient;

    private final CoordinatorConfiguration configuration;

    public ZooKeeperLeaderElectionAgent(CoordinatorConfiguration configuration){
        this.configuration = configuration;
        zooKeeperClient = ZooKeeperClient.getInstance();
    }

    @Override
    public void start(){
        logger.info("Starting ZooKeeper LeaderElection agent");
        zookeeper = zooKeeperClient.getClient();
        leaderLatch = new LeaderLatch(zookeeper, this.configuration.getLeaderElectionDir());
        leaderLatch.addListener(this);
        try {
            leaderLatch.start();
        } catch (Exception e) {
            logger.error("start leader latch error {0}",e);
        }

    }

    @Override
    public void stop() {
        try{
            leaderLatch.close();
            zookeeper.close();
        }catch (IOException e){
            logger.error("close leader latch error {}",e);
        }
    }

    @Override
    public void isLeader() {
        synchronized (this){
            logger.info("leader: "+leaderLatch.hasLeadership()+"");
            if(!leaderLatch.hasLeadership()){
                return;
            }

            logger.info("We have gained leadership");
            updateLeadershipStatus(true);
        }

    }

    @Override
    public void notLeader() {
        synchronized (this){
            logger.info("notLeader: "+leaderLatch.hasLeadership()+"");
            if(leaderLatch.hasLeadership()){
                return;
            }

            logger.info("We have lost leadership");
            updateLeadershipStatus(false);
        }
    }

    private void updateLeadershipStatus(boolean isLeader){
        if(isLeader){
            leaderElector.electedLeader();
        }else {
            leaderElector.revokedLeadership();
        }
    }

}
