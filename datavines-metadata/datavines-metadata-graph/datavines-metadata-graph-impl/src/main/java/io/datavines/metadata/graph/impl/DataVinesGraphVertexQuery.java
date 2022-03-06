package io.datavines.metadata.graph.impl;

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

import io.datavines.metadata.graph.api.IEdge;
import io.datavines.metadata.graph.api.IQuery;
import io.datavines.metadata.graph.api.IVertex;

public class DataVinesGraphVertexQuery implements IQuery<DataVinesVertex,DataVinesEdge> {

    private final DataVinesGraph DataVinesGraph;
    
    private final GraphTraversal<Vertex, Vertex> traversal;

    public DataVinesGraphVertexQuery(DataVinesGraph DataVinesGraph) {
        this.DataVinesGraph = DataVinesGraph;
        this.traversal = DataVinesGraph.getGraph().traversal().V();
    }

    @Override
    public Iterator<IEdge<DataVinesVertex, DataVinesEdge>> edges() {
        return IteratorUtils.transformedIterator(
                this.traversal.outE().toList().iterator(),
                edge -> GraphElementConverter.toDataVinesEdge(DataVinesGraph, edge));
    }

    @Override
    public Iterator<IEdge<DataVinesVertex, DataVinesEdge>> edges(long limit) {
        return IteratorUtils.transformedIterator(
                this.traversal.outE().limit(limit).toList().iterator(),
                edge -> GraphElementConverter.toDataVinesEdge(DataVinesGraph, edge));
    }

    @Override
    public Iterator<IEdge<DataVinesVertex, DataVinesEdge>> edges(long offset, long limit) {
        return null;
    }

    @Override
    public Iterator<IVertex<DataVinesVertex, DataVinesEdge>> vertices() {
        return IteratorUtils.transformedIterator(
                this.traversal.outV().toList().iterator(),
                vertex -> GraphElementConverter.toDataVinesVertex(DataVinesGraph, vertex));
    }

    @Override
    public Iterator<IVertex<DataVinesVertex, DataVinesEdge>> vertices(long limit) {
        return IteratorUtils.transformedIterator(
                this.traversal.outV().limit(limit).toList().iterator(),
                vertex -> GraphElementConverter.toDataVinesVertex(DataVinesGraph, vertex));
    }

    @Override
    public Iterator<IVertex<DataVinesVertex, DataVinesEdge>> vertices(long offset, long limit) {
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
    public IQuery<DataVinesVertex, DataVinesEdge> in(String property, Collection<?> values) {
        this.traversal.has(property, within(values));
        return this;
    }

    @Override
    public IQuery<DataVinesVertex, DataVinesEdge> has(String property, Object value) {
        this.traversal.has(property, value);
        return this;
    }

    @Override
    public IQuery<DataVinesVertex, DataVinesEdge> lt(String property, String value) {
        this.traversal.has(property, P.lt(value));
        return this;
    }

    @Override
    public IQuery<DataVinesVertex, DataVinesEdge> lte(String property, String value) {
        this.traversal.has(property, P.lte(value));
        return this;
    }

    @Override
    public IQuery<DataVinesVertex, DataVinesEdge> gt(String property, String value) {
        this.traversal.has(property, P.gt(value));
        return this;
    }

    @Override
    public IQuery<DataVinesVertex, DataVinesEdge> gte(String property, String value) {
        this.traversal.has(property, P.gte(value));
        return this;
    }

    @Override
    public IQuery<DataVinesVertex, DataVinesEdge> eq(String property, String value) {
        this.traversal.has(property, P.eq(value));
        return this;
    }

    @Override
    public IQuery<DataVinesVertex, DataVinesEdge> neq(String property, String value) {
        this.traversal.has(property, P.neq(value));
        return this;
    }

    @Override
    public IQuery<DataVinesVertex, DataVinesEdge> isNull(String value) {
        this.traversal.hasNot(value);
        return this;
    }

    @Override
    public IQuery<DataVinesVertex, DataVinesEdge> notNull(String value) {
        this.traversal.has(value);
        return this;
    }

    @Override
    public IQuery<DataVinesVertex, DataVinesEdge> startWith(String property, String value) {
        this.traversal.has(property,startingWith(value));
        return this;
    }

    @Override
    public IQuery<DataVinesVertex, DataVinesEdge> endWith(String property, String value) {
        this.traversal.has(property,endingWith(value));
        return this;
    }

    @Override
    public IQuery<DataVinesVertex, DataVinesEdge> notEndWith(String property, String value) {
        this.traversal.has(property,notEndingWith(value));
        return this;
    }

    @Override
    public IQuery<DataVinesVertex, DataVinesEdge> contains(String property, String value) {
        this.traversal.has(property,containing(value));
        return this;
    }

    @Override
    public IQuery<DataVinesVertex, DataVinesEdge> notContains(String property, String value) {
        this.traversal.has(property,notContaining(value));
        return this;
    }
}
