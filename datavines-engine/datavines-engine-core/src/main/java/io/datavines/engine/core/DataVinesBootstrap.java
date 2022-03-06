package io.datavines.engine.core;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.util.List;
import io.datavines.common.config.CheckResult;
import io.datavines.common.config.ConfigRuntimeException;
import io.datavines.engine.api.component.Component;
import io.datavines.engine.api.env.Execution;
import io.datavines.engine.api.env.RuntimeEnvironment;
import io.datavines.engine.core.command.CommandLineArgs;
import io.datavines.engine.core.config.ConfigParser;

/**
 * datavinesBootstrap
 */
public abstract class DataVinesBootstrap {

    private static final Logger logger = Logger.getLogger(io.datavines.engine.core.DataVinesBootstrap.class);

    public abstract CommandLineArgs getCommandLineArgs(String[] args);

    public void execute(String[] args) {

        CommandLineArgs commandLineArgs = getCommandLineArgs(args);
        if (commandLineArgs != null) {
            try {
                parseConfigAndExecute(commandLineArgs.getConfigFile());
            } catch (ConfigRuntimeException e) {
                showConfigError(e);
            } catch (Exception e) {
                showFatalError(e);
            }
        }
    }

    private void parseConfigAndExecute(String configFile) throws Exception {

        ConfigParser configParser = new ConfigParser(configFile);
        List<Component> sources = configParser.getSourcePlugins();
        List<Component> transforms = configParser.getTransformPlugins();
        List<Component> sinks = configParser.getSinkPlugins();
        Execution execution = configParser.getRuntimeEnvironment().getExecution();
        checkConfig(sources, transforms, sinks);
        prepare(configParser.getRuntimeEnvironment(), sources, transforms, sinks);
        if (execution == null) {
            throw new Exception("can not create execution , please check the config");
        }
        execution.execute(sources, transforms, sinks);
    }

    @SafeVarargs
    private final void checkConfig(List<? extends Component>... components) {
        boolean configValid = true;
        for (List<? extends Component> componentList : components) {
            for (Component component : componentList) {
                CheckResult checkResult = null;
                try {
                    checkResult = component.checkConfig();
                } catch (Exception e) {
                    checkResult = new CheckResult(false, e.getMessage());
                }

                if (!checkResult.isSuccess()) {
                    configValid = false;
                    logger.info(String.format("Component[%s] contains invalid config, error: %s\n"
                            , component.getClass().getName(), checkResult.getMsg()));
                }

                if (!configValid) {
                    // invalid configuration
                    System.exit(-1);
                }
            }
        }
    }

    @SafeVarargs
    private final void prepare(RuntimeEnvironment env, List<? extends Component>... components) {
        for (List<? extends Component> componentList : components) {
            componentList.forEach(component -> component.prepare(env));
        }
    }

    private void showConfigError(Throwable throwable) {
        logger.info(
                "\n\n===============================================================================\n\n");
        String errorMsg = throwable.getMessage();
        logger.info("Config Error:\n");
        logger.info("Reason: " + errorMsg + "\n");
        logger.info(
                "\n===============================================================================\n\n\n");
    }

    private void showFatalError(Throwable throwable) {
        logger.info(
                "\n\n===============================================================================\n\n");
        String errorMsg = throwable.getMessage();
        logger.info("Fatal Error, \n");
        logger.info("Reason: " + errorMsg + "\n");
        logger.info("Exception StackTrace: " + ExceptionUtils.getStackTrace(throwable));
        logger.info(
                "\n===============================================================================\n\n\n");
    }
}
