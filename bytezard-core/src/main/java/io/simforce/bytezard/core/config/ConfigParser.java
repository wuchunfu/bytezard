package io.simforce.bytezard.core.config;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import io.simforce.bytezard.common.config.BytezardConfiguration;
import io.simforce.bytezard.common.config.Config;
import io.simforce.bytezard.common.config.ConfigRuntimeException;
import io.simforce.bytezard.common.config.EnvConfig;
import io.simforce.bytezard.common.spi.PluginLoader;
import io.simforce.bytezard.common.utils.FileUtils;
import io.simforce.bytezard.common.utils.JsonUtils;
import io.simforce.bytezard.engine.api.component.Component;
import io.simforce.bytezard.engine.api.env.RuntimeEnvironment;

/**
 * ConfigParser
 */
public class ConfigParser {

    private static final Logger logger = Logger.getLogger(ConfigParser.class);

    private final String configFile;

    private final BytezardConfiguration config;

    private final EnvConfig envConfig;

    private final RuntimeEnvironment env;

    public ConfigParser(String configFile){
        this.configFile = configFile;
        this.config = load();
        this.envConfig = config.getEnvConfig();
        this.env = createRuntimeEnvironment();
    }

    private BytezardConfiguration load() {

        if (configFile.isEmpty()) {
            throw new ConfigRuntimeException("Please specify config file");
        }

        logger.info("[INFO] Loading config file: " + configFile);

        String configStr = FileUtils.readFile(configFile);
        BytezardConfiguration config = JsonUtils.fromJson(configStr,BytezardConfiguration.class);
        logger.info("[INFO] parsed config file: " + JsonUtils.toJson(config));
        return config;
    }

    private RuntimeEnvironment createRuntimeEnvironment() {
        RuntimeEnvironment env = PluginLoader
                .getPluginLoader(RuntimeEnvironment.class)
                .getNewPlugin(envConfig.getEngine());
        Config config = new Config(envConfig.getConfig());
        config.put("type",envConfig.getType());
        env.setConfig(config);
        env.prepare();
        return env;
    }

    public RuntimeEnvironment getRuntimeEnvironment() {
        return env;
    }

    public List<Component> getSourcePlugins() {
        List<Component> sourcePluginList = new ArrayList<>();
        config.getSourceParameters().forEach(sourceConfig -> {
            String pluginName = sourceConfig.getPlugin()+"-source";
            Component component = PluginLoader
                    .getPluginLoader(Component.class)
                    .getNewPlugin(pluginName);
            component.setConfig(new Config(sourceConfig.getConfig()));
            sourcePluginList.add(component);
        });
        return sourcePluginList;
    }

    public List<Component> getSinkPlugins() {
        List<Component> sinkPluginList = new ArrayList<>();
        config.getSinkParameters().forEach(sourceConfig -> {
            String pluginName = sourceConfig.getPlugin()+"-sink";
            Component component = PluginLoader
                    .getPluginLoader(Component.class)
                    .getNewPlugin(pluginName);
            component.setConfig(new Config(sourceConfig.getConfig()));
            sinkPluginList.add(component);
        });
        return sinkPluginList;
    }

    public List<Component> getTransformPlugins() {
        List<Component> transformPluginList = new ArrayList<>();
        config.getTransformParameters().forEach(sourceConfig -> {
            String pluginName = sourceConfig.getPlugin()+"-transform";
            Component component = PluginLoader
                    .getPluginLoader(Component.class)
                    .getNewPlugin(pluginName);
            component.setConfig(new Config(sourceConfig.getConfig()));
            transformPluginList.add(component);
        });
        return transformPluginList;
    }
}
