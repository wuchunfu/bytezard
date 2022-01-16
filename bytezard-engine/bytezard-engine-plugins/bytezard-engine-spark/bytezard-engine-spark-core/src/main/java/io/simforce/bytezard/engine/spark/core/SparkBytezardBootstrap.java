package io.simforce.bytezard.engine.spark.core;

import com.beust.jcommander.JCommander;

import io.simforce.bytezard.engine.core.BytezardBootstrap;
import io.simforce.bytezard.engine.core.command.CommandLineArgs;

public class SparkBytezardBootstrap extends BytezardBootstrap {

    public static void main(String[] args) {
        SparkBytezardBootstrap bootstrap = new SparkBytezardBootstrap();
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
