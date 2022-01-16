package io.simforce.bytezard.engine.executor.core.base;

import io.simforce.bytezard.common.utils.YarnUtils;

public abstract class AbstractHadoopEngineExecutor extends AbstractEngineExecutor {

    public AbstractHadoopEngineExecutor(){
    }

    protected boolean isSuccessOfYarnState(String[] appIds){
        return YarnUtils.isSuccessOfYarnState(appIds,this.cancel);
    }

    @Override
    public void cancel() throws Exception {

    }
}
