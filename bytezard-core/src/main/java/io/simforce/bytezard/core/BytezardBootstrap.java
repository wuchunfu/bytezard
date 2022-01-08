package io.simforce.bytezard.core;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

import io.simforce.bytezard.common.config.CheckResult;
import io.simforce.bytezard.common.config.ConfigRuntimeException;
import io.simforce.bytezard.core.config.ConfigParser;
import io.simforce.bytezard.engine.api.component.Component;
import io.simforce.bytezard.engine.api.env.Execution;
import io.simforce.bytezard.engine.api.env.RuntimeEnvironment;
import scala.Option;
import scala.collection.JavaConverters;
import scala.collection.Seq;
import scopt.OptionParser;

/**
 * BytezardBootstrap
 */
public class BytezardBootstrap {

    private static final Logger logger = Logger.getLogger(BytezardBootstrap.class);

    private final OptionParser<CommandLineArgs> parser;
    private final String[] args;

    public BytezardBootstrap(OptionParser<CommandLineArgs> parser, String[] args){
        this.parser = parser;
        this.args = args;
    }

    public static void main(String[] args) {
        OptionParser<CommandLineArgs> sparkParser = CommandLineUtils.sparkParser();
        BytezardBootstrap bootstrap = new BytezardBootstrap(sparkParser, args);
        bootstrap.execute();
    }

    public void execute() {
        Seq<String> seq = JavaConverters.asScalaIteratorConverter(Arrays.asList(args).iterator()).asScala().toSeq();
        Option<CommandLineArgs> option = parser.parse(seq,
                new CommandLineArgs("client", "application.conf", false));
        if (option.isDefined()) {
            CommandLineArgs commandLineArgs = option.get();
            Common.setDeployMode(commandLineArgs.deployMode());
            String configFilePath = getConfigFilePath(commandLineArgs);
            try {
                parseConfigAndExecute(configFilePath);
            } catch (ConfigRuntimeException e) {
                showConfigError(e);
            }catch (Exception e){
                showFatalError(e);
            }
        }
    }

    private String getConfigFilePath(CommandLineArgs cmdArgs) {
        String path = null;

        final Option<String> mode = Common.getDeployMode();
        if (mode.isDefined() && "cluster".equals(mode.get())) {
            path = new Path(cmdArgs.configFile()).getName();
        } else {
            path = cmdArgs.configFile();
        }
        return path;
    }

    private void parseConfigAndExecute(String configFile) throws Exception {

        ConfigParser configParser = new ConfigParser(configFile);
        List<Component> sources = configParser.getSourcePlugins();
        List<Component> transforms = configParser.getTransformPlugins();
        List<Component> sinks = configParser.getSinkPlugins();
        Execution execution = configParser.getRuntimeEnvironment().getExecution();
        checkConfig(sources, transforms, sinks);
        prepare(configParser.getRuntimeEnvironment(), sources, transforms, sinks);
        if(execution == null){
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
