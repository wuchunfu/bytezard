package io.datavines.metadata.graph.impl;

import org.apache.tinkerpop.gremlin.structure.Edge;

import io.datavines.metadata.graph.api.IEdge;
import io.datavines.metadata.graph.api.IVertex;

public class DataVinesEdge extends DataVinesElement<Edge> implements IEdge<DataVinesVertex, DataVinesEdge> {

    public DataVinesEdge(DataVinesGraph graph, Edge element) {
        super(graph, element);
    }

    @Override
    public IVertex<DataVinesVertex, DataVinesEdge> getInVertex() {
        return GraphElementConverter.toDataVinesVertex(graph, getOriginalElement().inVertex());
    }

    @Override
    public IVertex<DataVinesVertex, DataVinesEdge> getOutVertex() {
        return GraphElementConverter.toDataVinesVertex(graph, getOriginalElement().outVertex());
    }

    @Override
    public String getLabel() {
        return getOriginalElement().label();
    }

    @Override
    public DataVinesEdge getE() {
        return this;
    }
}
