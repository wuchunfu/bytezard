package io.datavines.engine.executor.spark;

import org.slf4j.Logger;

import io.datavines.common.config.DataVinesConfiguration;
import io.datavines.common.entity.TaskRequest;
import io.datavines.common.entity.ProcessResult;
import io.datavines.common.utils.JSONUtils;
import io.datavines.engine.api.engine.EngineExecutor;
import io.datavines.engine.api.parser.ConfigurationParser;
import io.datavines.engine.executor.spark.parser.SparkConfigurationParser;

public class SparkEngineExecutor implements EngineExecutor {

    private final ConfigurationParser<DataVinesConfiguration> configurationParser
            = new SparkConfigurationParser();

    private TaskRequest taskRequest;

    @Override
    public void init() throws Exception {
        //读取每个引擎特有参数
        //读取数据质量检查标准参数
        DataVinesConfiguration configuration =
                JSONUtils.parseObject(taskRequest.getEngineParameter(),DataVinesConfiguration.class);
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
    public void setTaskRequest(TaskRequest taskRequest) {
        this.taskRequest = taskRequest;
    }

    @Override
    public TaskRequest getTaskRequest() {
        return null;
    }

}
