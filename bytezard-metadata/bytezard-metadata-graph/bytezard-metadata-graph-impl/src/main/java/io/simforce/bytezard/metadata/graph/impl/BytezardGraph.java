package io.simforce.bytezard.metadata.graph.impl;

import static io.simforce.bytezard.metadata.graph.impl.Constants.STATE_PROPERTY_KEY;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Iterator;

import io.simforce.bytezard.metadata.graph.api.IEdge;
import io.simforce.bytezard.metadata.graph.api.IGraph;
import io.simforce.bytezard.metadata.graph.api.IVertex;
import io.simforce.bytezard.metadata.graph.enums.Status;

public class BytezardGraph implements IGraph<BytezardVertex, BytezardEdge> {

    private final Graph graph;

    public BytezardGraph(Graph graph) {
        this.graph = graph;
    }

    @Override
    public IEdge<BytezardVertex, BytezardEdge> addEdge(IVertex<BytezardVertex, BytezardEdge> outVertex, IVertex<BytezardVertex, BytezardEdge> inVertex, String label) {
        Vertex oV = outVertex.getV().getOriginalElement();
        Vertex iV = inVertex.getV().getOriginalElement();
        Edge edge = oV.addEdge(label, iV);
        return GraphElementConverter.toBytezardEdge(this, edge);
    }

    @Override
    public IEdge<BytezardVertex, BytezardEdge> getEdge(IVertex<BytezardVertex, BytezardEdge> fromVertex, IVertex<BytezardVertex, BytezardEdge> toVertex, String relationshipLabel) {
        GraphTraversal<Vertex, Edge> gt = graph.traversal()
                .V(fromVertex.getId())
                .outE(relationshipLabel)
                .where(__.otherV().hasId(toVertex.getId()));

        Edge edge = getFirstActiveEdge(gt);
        return (edge != null)
                ? GraphElementConverter.toBytezardEdge(this, edge)
                : null;
    }

    @Override
    public IVertex<BytezardVertex, BytezardEdge> addVertex() {
        return GraphElementConverter.toBytezardVertex(this, graph.addVertex());
    }

    @Override
    public void removeEdge(IEdge<BytezardVertex, BytezardEdge> edge) {
       edge.getE().getOriginalElement().remove();
    }

    @Override
    public void removeVertex(IVertex<BytezardVertex, BytezardEdge> vertex) {
        vertex.getV().getOriginalElement().remove();
    }

    @Override
    public IEdge<BytezardVertex, BytezardEdge> getEdge(String edgeId) {
        Iterator<Edge> it = graph.edges(edgeId);
        Edge edge = getSingleElement(it, edgeId);

        return GraphElementConverter.toBytezardEdge(this, edge);
    }

    @Override
    public Iterator<IEdge<BytezardVertex, BytezardEdge>> getEdges() {
        Iterator<Edge> edges = graph.edges();
        return IteratorUtils.transformedIterator(edges,
                edge -> GraphElementConverter.toBytezardEdge(this, edge));
    }

    @Override
    public Iterator<IVertex<BytezardVertex, BytezardEdge>> getVertices() {
        Iterator<Vertex> vertices = graph.vertices();
        return IteratorUtils.transformedIterator(vertices,
                vertex -> GraphElementConverter.toBytezardVertex(this, vertex));
    }

    @Override
    public IVertex<BytezardVertex, BytezardEdge> getVertex(String vertexId) {
        Iterator<Vertex> it = graph.vertices(vertexId);
        Vertex vertex = getSingleElement(it, vertexId);

        return GraphElementConverter.toBytezardVertex(this, vertex);
    }

    @Override
    public Iterator<IVertex<BytezardVertex, BytezardEdge>> getVertices(String property, Object value) {
        Iterator<Vertex> vertices = graph.traversal().V().has(property, P.lt(value));
        return IteratorUtils.transformedIterator(vertices,
                vertex -> GraphElementConverter.toBytezardVertex(this, vertex));
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
