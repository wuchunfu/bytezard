package io.simforce.bytezard.engine.api.engine;

import org.slf4j.Logger;

import io.simforce.bytezard.common.entity.ExecutionJob;
import io.simforce.bytezard.common.entity.ProcessResult;
import io.simforce.bytezard.common.spi.SPI;

@SPI
public interface EngineExecutor {

    /**
     * 进行初始化操作
     * @throws Exception Exception
     */
    void init() throws Exception;

    /**
     * 执行实际内容
     * @throws Exception Exception
     */
    void execute() throws Exception;

    /**
     * 做好任务执行完之后的处理工作
     * @throws Exception Exception
     */
    void after() throws Exception;

    /**
     * 取消任务
     * @throws Exception Exception
     */
    void cancel() throws Exception;

    /**
     * 是否取消
     * @throws Exception Exception
     */
    boolean isCancel() throws Exception;

    /**
     * 获取执行结果
     */
    ProcessResult getProcessResult();

    /**
     * 设置logger
     * @param logger
     */
    void setLogger(Logger logger);

    /**
     * 设置execution job
     * @param job
     */
    void setExecutionJob(ExecutionJob job);

    /**
     * 获取execution job
     * @return
     */
    ExecutionJob getExecutionJob();
}
