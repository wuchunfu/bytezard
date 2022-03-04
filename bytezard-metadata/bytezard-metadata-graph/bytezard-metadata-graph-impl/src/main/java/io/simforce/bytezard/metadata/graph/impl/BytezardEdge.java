package io.simforce.bytezard.metadata.graph.impl;

import org.apache.tinkerpop.gremlin.structure.Edge;

import io.simforce.bytezard.metadata.graph.api.IEdge;
import io.simforce.bytezard.metadata.graph.api.IVertex;

public class BytezardEdge extends BytezardElement<Edge> implements IEdge<BytezardVertex, BytezardEdge> {

    public BytezardEdge(BytezardGraph graph, Edge element) {
        super(graph, element);
    }

    @Override
    public IVertex<BytezardVertex, BytezardEdge> getInVertex() {
        return GraphElementConverter.toBytezardVertex(graph, getOriginalElement().inVertex());
    }

    @Override
    public IVertex<BytezardVertex, BytezardEdge> getOutVertex() {
        return GraphElementConverter.toBytezardVertex(graph, getOriginalElement().outVertex());
    }

    @Override
    public String getLabel() {
        return getOriginalElement().label();
    }

    @Override
    public BytezardEdge getE() {
        return this;
    }
}
