package io.simforce.bytezard.coordinator.server.processor;

import io.simforce.bytezard.coordinator.server.cache.JobExecuteManager;
import io.simforce.bytezard.remote.command.Command;
import io.simforce.bytezard.remote.processor.NettyEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

/**
 * @author zixi0825
 */
public class JobKillResponseProcessor implements NettyEventProcessor {

    private final Logger logger = LoggerFactory.getLogger(JobKillResponseProcessor.class);

    private final JobExecuteManager jobExecuteManager;

    public JobKillResponseProcessor(JobExecuteManager jobExecuteManager){
        this.jobExecuteManager = jobExecuteManager;
    }

    @Override
    public void process(Channel channel, Command command) {

    }
}
