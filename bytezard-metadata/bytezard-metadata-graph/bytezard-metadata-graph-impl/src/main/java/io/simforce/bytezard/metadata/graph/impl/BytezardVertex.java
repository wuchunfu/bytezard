package io.simforce.bytezard.metadata.graph.impl;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.StreamSupport;

import io.simforce.bytezard.metadata.graph.api.EdgeDirection;
import io.simforce.bytezard.metadata.graph.api.IEdge;
import io.simforce.bytezard.metadata.graph.api.IVertex;
import io.simforce.bytezard.metadata.graph.impl.utils.IteratorToIterableAdapter;

/**
 * @author zixi0825
 */
public class BytezardVertex extends BytezardElement<Vertex> implements IVertex<BytezardVertex, BytezardEdge> {

    public BytezardVertex(BytezardGraph graph, Vertex element) {
        super(graph, element);
    }

    @Override
    public Iterator<IEdge<BytezardVertex, BytezardEdge>> getEdges(EdgeDirection direction, String edgeLabel) {
        Direction d = GraphElementConverter.toDirection(direction);
        Iterator<Edge> edges = getOriginalElement().edges(d, edgeLabel);
        return IteratorUtils.transformedIterator(edges, edge -> GraphElementConverter.toBytezardEdge(graph, edge));
    }

    @Override
    public Iterator<IEdge<BytezardVertex, BytezardEdge>> getEdges(EdgeDirection direction, String[] edgeLabels) {
        Direction d = GraphElementConverter.toDirection(direction);
        Iterator<Edge> edges = getOriginalElement().edges(d, edgeLabels);
        return IteratorUtils.transformedIterator(edges, edge -> GraphElementConverter.toBytezardEdge(graph, edge));
    }

    @Override
    public long getEdgesCount(EdgeDirection direction, String edgeLabel) {
        Direction d = GraphElementConverter.toDirection(direction);
        Iterator<Edge> edges = getOriginalElement().edges(d, edgeLabel);
        IteratorToIterableAdapter<Edge> iterable = new IteratorToIterableAdapter<>(edges);
        return StreamSupport.stream(iterable.spliterator(), true).count();
    }

    @Override
    public boolean hasEdges(EdgeDirection direction, String edgeLabel) {
        Direction d = GraphElementConverter.toDirection(direction);
        Iterator<Edge> edges = getOriginalElement().edges(d, edgeLabel);
        return edges.hasNext();
    }

    @Override
    public Iterator<IEdge<BytezardVertex, BytezardEdge>> getEdges(EdgeDirection direction) {
        Direction d = GraphElementConverter.toDirection(direction);
        Iterator<Edge> edges = getOriginalElement().edges(d);
        return IteratorUtils.transformedIterator(edges, edge -> GraphElementConverter.toBytezardEdge(graph, edge));
    }

    @Override
    public <T> void addProperty(String propertyName, T value) {
        getOriginalElement().property(VertexProperty.Cardinality.set, propertyName, value);
    }

    @Override
    public <T> void addListProperty(String propertyName, T value) {
        getOriginalElement().property(VertexProperty.Cardinality.list, propertyName, value);
    }

    @Override
    public <T> Collection<T> getPropertyValues(String propertyName, Class<T> clazz) {

        Collection<T> result = new ArrayList<T>();
        Iterator<VertexProperty<T>> it = getOriginalElement().properties(propertyName);
        while(it.hasNext()) {
            result.add(it.next().value());
        }

        return result;
    }

    @Override
    public BytezardVertex getV() {
        return this;
    }
}
