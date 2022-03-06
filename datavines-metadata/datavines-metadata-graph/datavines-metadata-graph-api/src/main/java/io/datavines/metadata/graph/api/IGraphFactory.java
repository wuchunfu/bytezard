package io.datavines.metadata.graph.api;

import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.structure.Graph;

import io.datavines.spi.SPI;

@SPI
public interface IGraphFactory {
    Graph getGraph(Configuration configuration);
}
