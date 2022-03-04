package io.simforce.bytezard.metadata.graph.impl;

import static org.apache.tinkerpop.gremlin.process.traversal.P.within;
import static org.apache.tinkerpop.gremlin.process.traversal.TextP.containing;
import static org.apache.tinkerpop.gremlin.process.traversal.TextP.endingWith;
import static org.apache.tinkerpop.gremlin.process.traversal.TextP.notContaining;
import static org.apache.tinkerpop.gremlin.process.traversal.TextP.notEndingWith;
import static org.apache.tinkerpop.gremlin.process.traversal.TextP.startingWith;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Collection;
import java.util.Iterator;

import io.simforce.bytezard.metadata.graph.api.IEdge;
import io.simforce.bytezard.metadata.graph.api.IQuery;
import io.simforce.bytezard.metadata.graph.api.IVertex;

public class BytezardGraphVertexQuery implements IQuery<BytezardVertex,BytezardEdge> {

    private final BytezardGraph bytezardGraph;
    
    private final GraphTraversal<Vertex, Vertex> traversal;

    public BytezardGraphVertexQuery(BytezardGraph bytezardGraph) {
        this.bytezardGraph = bytezardGraph;
        this.traversal = bytezardGraph.getGraph().traversal().V();
    }

    @Override
    public Iterator<IEdge<BytezardVertex, BytezardEdge>> edges() {
        return IteratorUtils.transformedIterator(
                this.traversal.outE().toList().iterator(),
                edge -> GraphElementConverter.toBytezardEdge(bytezardGraph, edge));
    }

    @Override
    public Iterator<IEdge<BytezardVertex, BytezardEdge>> edges(long limit) {
        return IteratorUtils.transformedIterator(
                this.traversal.outE().limit(limit).toList().iterator(),
                edge -> GraphElementConverter.toBytezardEdge(bytezardGraph, edge));
    }

    @Override
    public Iterator<IEdge<BytezardVertex, BytezardEdge>> edges(long offset, long limit) {
        return null;
    }

    @Override
    public Iterator<IVertex<BytezardVertex, BytezardEdge>> vertices() {
        return IteratorUtils.transformedIterator(
                this.traversal.outV().toList().iterator(),
                vertex -> GraphElementConverter.toBytezardVertex(bytezardGraph, vertex));
    }

    @Override
    public Iterator<IVertex<BytezardVertex, BytezardEdge>> vertices(long limit) {
        return IteratorUtils.transformedIterator(
                this.traversal.outV().limit(limit).toList().iterator(),
                vertex -> GraphElementConverter.toBytezardVertex(bytezardGraph, vertex));
    }

    @Override
    public Iterator<IVertex<BytezardVertex, BytezardEdge>> vertices(long offset, long limit) {
        return null;
    }

    @Override
    public Iterator<Object> vertexIds() {
        return IteratorUtils.transformedIterator(
                this.traversal.outV().toList().iterator(), Vertex::id);
    }

    @Override
    public Iterator<Object> vertexIds(long limit) {
        return IteratorUtils.transformedIterator(
                this.traversal.outV().limit(limit).toList().iterator(), Vertex::id);
    }

    @Override
    public Iterator<Object> vertexIds(long offset, long limit) {
        return null;
    }

    @Override
    public IQuery<BytezardVertex, BytezardEdge> in(String property, Collection<?> values) {
        this.traversal.has(property, within(values));
        return this;
    }

    @Override
    public IQuery<BytezardVertex, BytezardEdge> has(String property, Object value) {
        this.traversal.has(property, value);
        return this;
    }

    @Override
    public IQuery<BytezardVertex, BytezardEdge> lt(String property, String value) {
        this.traversal.has(property, P.lt(value));
        return this;
    }

    @Override
    public IQuery<BytezardVertex, BytezardEdge> lte(String property, String value) {
        this.traversal.has(property, P.lte(value));
        return this;
    }

    @Override
    public IQuery<BytezardVertex, BytezardEdge> gt(String property, String value) {
        this.traversal.has(property, P.gt(value));
        return this;
    }

    @Override
    public IQuery<BytezardVertex, BytezardEdge> gte(String property, String value) {
        this.traversal.has(property, P.gte(value));
        return this;
    }

    @Override
    public IQuery<BytezardVertex, BytezardEdge> eq(String property, String value) {
        this.traversal.has(property, P.eq(value));
        return this;
    }

    @Override
    public IQuery<BytezardVertex, BytezardEdge> neq(String property, String value) {
        this.traversal.has(property, P.neq(value));
        return this;
    }

    @Override
    public IQuery<BytezardVertex, BytezardEdge> isNull(String value) {
        this.traversal.hasNot(value);
        return this;
    }

    @Override
    public IQuery<BytezardVertex, BytezardEdge> notNull(String value) {
        this.traversal.has(value);
        return this;
    }

    @Override
    public IQuery<BytezardVertex, BytezardEdge> startWith(String property, String value) {
        this.traversal.has(property,startingWith(value));
        return this;
    }

    @Override
    public IQuery<BytezardVertex, BytezardEdge> endWith(String property, String value) {
        this.traversal.has(property,endingWith(value));
        return this;
    }

    @Override
    public IQuery<BytezardVertex, BytezardEdge> notEndWith(String property, String value) {
        this.traversal.has(property,notEndingWith(value));
        return this;
    }

    @Override
    public IQuery<BytezardVertex, BytezardEdge> contains(String property, String value) {
        this.traversal.has(property,containing(value));
        return this;
    }

    @Override
    public IQuery<BytezardVertex, BytezardEdge> notContains(String property, String value) {
        this.traversal.has(property,notContaining(value));
        return this;
    }
}
