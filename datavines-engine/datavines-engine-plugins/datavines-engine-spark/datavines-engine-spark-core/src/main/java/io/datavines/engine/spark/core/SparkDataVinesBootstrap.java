package io.datavines.engine.spark.core;

import com.beust.jcommander.JCommander;

import io.datavines.engine.core.DataVinesBootstrap;
import io.datavines.engine.core.command.CommandLineArgs;

public class SparkDataVinesBootstrap extends DataVinesBootstrap {

    public static void main(String[] args) {
        SparkDataVinesBootstrap bootstrap = new SparkDataVinesBootstrap();
        bootstrap.execute(args);
    }

    @Override
    public CommandLineArgs getCommandLineArgs(String[] args) {
        CommandSparkArgs commandSparkArgs = new CommandSparkArgs();
        JCommander.newBuilder()
                .addObject(commandSparkArgs)
                .build()
                .parse(args);

        return new CommandLineArgs(
                commandSparkArgs.getDeployMode(),
                commandSparkArgs.getConfigFile(),
                commandSparkArgs.isTestConfig()
        );
    }
}
