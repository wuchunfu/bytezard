package io.simforce.bytezard.metadata.graph.api;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.util.Iterator;
import java.util.Set;

/**
 * @author zixi0825
 */
public interface IGraph<V,E> {

    /**
     * Adds an edge to the graph.
     *
     * @param outVertex
     * @param inVertex
     * @param label
     * @return
     */
    IEdge<V, E> addEdge(IVertex<V, E> outVertex, IVertex<V, E> inVertex, String label);

    /**
     * Fetch edges between two vertices using relationshipLabel
     * @param fromVertex
     * @param toVertex
     * @param relationshipLabel
     * @return
     */
    IEdge<V, E> getEdge(IVertex<V,E> fromVertex, IVertex<V,E> toVertex, String relationshipLabel);

    /**
     * Adds a vertex to the graph.
     *
     * @return
     */
    IVertex<V, E> addVertex();

    /**
     * Removes the specified edge from the graph.
     *
     * @param edge
     */
    void removeEdge(IEdge<V, E> edge);

    /**
     * Removes the specified vertex from the graph.
     *
     * @param vertex
     */
    void removeVertex(IVertex<V, E> vertex);

    /**
     * Retrieves the edge with the specified id.    As an optimization, a non-null Edge may be
     * returned by some implementations if the Edge does not exist.  In that case,
     * you can call {@link IElement} to determine whether the vertex
     * exists.  This allows the retrieval of the Edge information to be deferred
     * or in come cases avoided altogether in implementations where that might
     * be an expensive operation.
     *
     * @param edgeId
     * @return
     */
    IEdge<V, E> getEdge(String edgeId);

    /**
     * Gets all the edges in the graph.
     * @return
     */
    Iterator<IEdge<V, E>> getEdges();

    /**
     * Gets all the vertices in the graph.
     * @return
     */
    Iterator<IVertex<V, E>> getVertices();

    /**
     * Gets the vertex with the specified id.  As an optimization, a non-null vertex may be
     * returned by some implementations if the Vertex does not exist.  In that case,
     * you can call {@link IElement} to determine whether the vertex
     * exists.  This allows the retrieval of the Vertex information to be deferred
     * or in come cases avoided altogether in implementations where that might
     * be an expensive operation.
     *
     * @param vertexId
     * @return
     */
    IVertex<V, E> getVertex(String vertexId);

    /**
     * Finds the vertices where the given property key
     * has the specified value.  For multi-valued properties,
     * finds the vertices where the value list contains
     * the specified value.
     *
     * @param key
     * @param value
     * @return
     */
    Iterator<IVertex<V, E>> getVertices(String key, Object value);

    /**
     * Commits changes made to the graph in the current transaction.
     */
    void commit();

    /**
     * Rolls back changes made to the graph in the current transaction.
     */
    void rollback();

    /**
     * Unloads and releases any resources associated with the graph.
     */
    void close() throws Exception;

    Graph getGraph();

    GraphTraversalSource getGraphTraversalSource();
    
}
