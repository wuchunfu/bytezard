package io.simforce.bytezard.metadata.repository;

import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.structure.Graph;

import io.simforce.bytezard.metadata.graph.api.GraphFactory;
import io.simforce.bytezard.spi.PluginLoader;

public class GraphManager {

    public static Graph getGraph(String type, Configuration configuration) {
        return PluginLoader
                .getPluginLoader(GraphFactory.class)
                .getOrCreatePlugin(type)
                .getGraph(configuration);
    }
}
