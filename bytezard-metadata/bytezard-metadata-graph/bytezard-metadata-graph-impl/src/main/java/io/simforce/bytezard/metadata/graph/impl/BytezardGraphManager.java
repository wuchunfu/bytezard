package io.simforce.bytezard.metadata.graph.impl;

import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.structure.Graph;

import io.simforce.bytezard.metadata.graph.api.IGraph;
import io.simforce.bytezard.metadata.graph.api.IGraphFactory;
import io.simforce.bytezard.spi.PluginLoader;

public class BytezardGraphManager {

    public static Graph getGraph(String type, Configuration configuration) {
        return PluginLoader
                .getPluginLoader(IGraphFactory.class)
                .getOrCreatePlugin(type)
                .getGraph(configuration);
    }
}
