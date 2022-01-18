package io.simforce.bytezard.engine.executor.core.base;

import java.util.List;

import org.slf4j.Logger;

import io.simforce.bytezard.common.entity.TaskRequest;
import io.simforce.bytezard.common.entity.ProcessResult;
import io.simforce.bytezard.common.parameter.AbstractParameters;
import io.simforce.bytezard.engine.api.engine.EngineExecutor;

public abstract class AbstractEngineExecutor implements EngineExecutor {

    protected TaskRequest taskRequest;

    protected Logger logger;

    protected volatile boolean cancel = false;

    protected ProcessResult processResult;

    public AbstractEngineExecutor() {

    }

    /**
     * log handle
     * @param logs log list
     */
    public void logHandle(List<String> logs) {
        // note that the "new line" is added here to facilitate log parsing
        logger.info(" -> {}", String.join("\n\t", logs));
    }

    /**
     * @return AbstractParameters
     */
    public abstract AbstractParameters getParameters();

    @Override
    public boolean isCancel(){
        return cancel;
    }
}
