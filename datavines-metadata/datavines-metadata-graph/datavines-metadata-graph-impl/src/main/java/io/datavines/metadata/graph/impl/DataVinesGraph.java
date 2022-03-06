package io.datavines.metadata.graph.impl;

import static io.datavines.metadata.graph.impl.Constants.STATE_PROPERTY_KEY;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Iterator;

import io.datavines.metadata.graph.api.IEdge;
import io.datavines.metadata.graph.api.IGraph;
import io.datavines.metadata.graph.api.IVertex;
import io.datavines.metadata.graph.enums.Status;

public class DataVinesGraph implements IGraph<DataVinesVertex, DataVinesEdge> {

    private final Graph graph;

    public DataVinesGraph(Graph graph) {
        this.graph = graph;
    }

    @Override
    public IEdge<DataVinesVertex, DataVinesEdge> addEdge(IVertex<DataVinesVertex, DataVinesEdge> outVertex, IVertex<DataVinesVertex, DataVinesEdge> inVertex, String label) {
        Vertex oV = outVertex.getV().getOriginalElement();
        Vertex iV = inVertex.getV().getOriginalElement();
        Edge edge = oV.addEdge(label, iV);
        return GraphElementConverter.toDataVinesEdge(this, edge);
    }

    @Override
    public IEdge<DataVinesVertex, DataVinesEdge> getEdge(IVertex<DataVinesVertex, DataVinesEdge> fromVertex, IVertex<DataVinesVertex, DataVinesEdge> toVertex, String relationshipLabel) {
        GraphTraversal<Vertex, Edge> gt = graph.traversal()
                .V(fromVertex.getId())
                .outE(relationshipLabel)
                .where(__.otherV().hasId(toVertex.getId()));

        Edge edge = getFirstActiveEdge(gt);
        return (edge != null)
                ? GraphElementConverter.toDataVinesEdge(this, edge)
                : null;
    }

    @Override
    public IVertex<DataVinesVertex, DataVinesEdge> addVertex() {
        return GraphElementConverter.toDataVinesVertex(this, graph.addVertex());
    }

    @Override
    public void removeEdge(IEdge<DataVinesVertex, DataVinesEdge> edge) {
       edge.getE().getOriginalElement().remove();
    }

    @Override
    public void removeVertex(IVertex<DataVinesVertex, DataVinesEdge> vertex) {
        vertex.getV().getOriginalElement().remove();
    }

    @Override
    public IEdge<DataVinesVertex, DataVinesEdge> getEdge(String edgeId) {
        Iterator<Edge> it = graph.edges(edgeId);
        Edge edge = getSingleElement(it, edgeId);

        return GraphElementConverter.toDataVinesEdge(this, edge);
    }

    @Override
    public Iterator<IEdge<DataVinesVertex, DataVinesEdge>> getEdges() {
        Iterator<Edge> edges = graph.edges();
        return IteratorUtils.transformedIterator(edges,
                edge -> GraphElementConverter.toDataVinesEdge(this, edge));
    }

    @Override
    public Iterator<IVertex<DataVinesVertex, DataVinesEdge>> getVertices() {
        Iterator<Vertex> vertices = graph.vertices();
        return IteratorUtils.transformedIterator(vertices,
                vertex -> GraphElementConverter.toDataVinesVertex(this, vertex));
    }

    @Override
    public IVertex<DataVinesVertex, DataVinesEdge> getVertex(String vertexId) {
        Iterator<Vertex> it = graph.vertices(vertexId);
        Vertex vertex = getSingleElement(it, vertexId);

        return GraphElementConverter.toDataVinesVertex(this, vertex);
    }

    @Override
    public Iterator<IVertex<DataVinesVertex, DataVinesEdge>> getVertices(String property, Object value) {
        Iterator<Vertex> vertices = graph.traversal().V().has(property, P.lt(value));
        return IteratorUtils.transformedIterator(vertices,
                vertex -> GraphElementConverter.toDataVinesVertex(this, vertex));
    }

    @Override
    public void commit() {
        this.graph.tx().commit();
    }

    @Override
    public void rollback() {
        this.graph.tx().rollback();
    }

    @Override
    public void close() throws Exception {
        this.graph.close();
    }

    @Override
    public Graph getGraph() {
        return this.graph;
    }

    @Override
    public GraphTraversalSource getGraphTraversalSource() {
        return this.graph.traversal();
    }

    private Edge getFirstActiveEdge(GraphTraversal<Vertex, Edge> gt) {
        while (gt.hasNext()) {
            Edge gremlinEdge = gt.next();
            if (gremlinEdge != null && gremlinEdge.property(STATE_PROPERTY_KEY).isPresent() &&
                    gremlinEdge.property(STATE_PROPERTY_KEY).value().equals(Status.ACTIVE.name())
            ) {
                return gremlinEdge;
            }
        }

        return null;
    }

    private static <T> T getSingleElement(Iterator<T> it, String id) {
        if (!it.hasNext()) {
            return null;
        }

        T element = it.next();

        if (it.hasNext()) {
            throw new RuntimeException("Multiple items were found with the id " + id);
        }

        return element;
    }
}
