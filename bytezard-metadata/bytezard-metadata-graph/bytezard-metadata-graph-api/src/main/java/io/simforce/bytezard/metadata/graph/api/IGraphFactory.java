package io.simforce.bytezard.metadata.graph.api;

import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.structure.Graph;

import io.simforce.bytezard.spi.SPI;

@SPI
public interface IGraphFactory {
    Graph getGraph(Configuration configuration);
}
