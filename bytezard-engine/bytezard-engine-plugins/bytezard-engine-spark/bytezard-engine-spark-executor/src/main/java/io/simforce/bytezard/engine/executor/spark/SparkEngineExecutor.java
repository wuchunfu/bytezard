package io.simforce.bytezard.engine.executor.spark;

import org.slf4j.Logger;

import io.simforce.bytezard.common.config.BytezardConfiguration;
import io.simforce.bytezard.common.entity.ExecutionJob;
import io.simforce.bytezard.common.entity.ProcessResult;
import io.simforce.bytezard.common.utils.JSONUtils;
import io.simforce.bytezard.engine.api.engine.EngineExecutor;
import io.simforce.bytezard.engine.api.parser.ConfigurationParser;
import io.simforce.bytezard.engine.executor.spark.parser.SparkConfigurationParser;

public class SparkEngineExecutor implements EngineExecutor {

    private final ConfigurationParser<BytezardConfiguration> configurationParser
            = new SparkConfigurationParser();

    private ExecutionJob executionJob;

    @Override
    public void init() throws Exception {
        //读取每个引擎特有参数
        //读取数据质量检查标准参数
        BytezardConfiguration configuration =
                JSONUtils.parseObject(executionJob.getBytezardConfiguration(),BytezardConfiguration.class);
        configurationParser.parse(configuration);
        //将标准参数转换为引擎可执行参数,生成一个application.conf
        //执行检查任务
    }

    @Override
    public void execute() throws Exception {

    }

    @Override
    public void after() throws Exception {

    }

    @Override
    public void cancel() throws Exception {

    }

    @Override
    public boolean isCancel() throws Exception {
        return false;
    }

    @Override
    public ProcessResult getProcessResult() {
        return null;
    }

    @Override
    public void setLogger(Logger logger) {

    }

    @Override
    public void setExecutionJob(ExecutionJob executionJob) {
        this.executionJob = executionJob;
    }

    @Override
    public ExecutionJob getExecutionJob() {
        return null;
    }

}
