package io.simforce.bytezard.executor.processor;

import io.simforce.bytezard.executor.cache.JobExecutionCache;
import io.simforce.bytezard.executor.cache.JobExecutionContext;
import io.simforce.bytezard.executor.runner.TaskRunner;
import io.simforce.bytezard.remote.command.Command;
import io.simforce.bytezard.remote.command.CommandCode;
import io.simforce.bytezard.remote.command.JobKillRequestCommand;
import io.simforce.bytezard.remote.processor.NettyEventProcessor;
import io.simforce.bytezard.remote.utils.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.netty.channel.Channel;

public class JobKillProcessor implements NettyEventProcessor {

    private final Logger logger = LoggerFactory.getLogger(JobKillProcessor.class);

    private final JobExecutionCache jobExecutionCache;

    public JobKillProcessor(){
        this.jobExecutionCache = JobExecutionCache.getInstance();
    }

    @Override
    public void process(Channel channel, Command command) {
        Preconditions.checkArgument(CommandCode.JOB_KILL_REQUEST == command.getCode(),
                String.format("invalid command type : %s", command.getCode()));

        JobKillRequestCommand jobKillRequestCommand = JsonSerializer.deserialize(command.getBody(),JobKillRequestCommand.class);
        Long taskId = jobKillRequestCommand.getTaskId();
        JobExecutionContext jobExecutionContext = jobExecutionCache.getById(taskId);

        if (jobExecutionContext != null){
            TaskRunner taskRunner = jobExecutionContext.getTaskRunner();
            taskRunner.kill();
        }

    }
}
