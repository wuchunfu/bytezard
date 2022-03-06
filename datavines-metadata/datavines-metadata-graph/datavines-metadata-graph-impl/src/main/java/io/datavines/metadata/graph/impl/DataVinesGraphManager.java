package io.datavines.metadata.graph.impl;

import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.structure.Graph;

import io.datavines.metadata.graph.api.IGraphFactory;
import io.datavines.spi.PluginLoader;

public class DataVinesGraphManager {

    public static Graph getGraph(String type, Configuration configuration) {
        return PluginLoader
                .getPluginLoader(IGraphFactory.class)
                .getOrCreatePlugin(type)
                .getGraph(configuration);
    }
}
