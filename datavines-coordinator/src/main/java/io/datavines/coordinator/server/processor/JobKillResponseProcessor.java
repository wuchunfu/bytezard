package io.datavines.coordinator.server.processor;

import io.datavines.coordinator.server.cache.JobExecuteManager;
import io.datavines.remote.command.Command;
import io.datavines.remote.processor.NettyEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

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
