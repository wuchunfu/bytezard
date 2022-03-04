package io.simforce.bytezard.metadata.graph.api;

import java.util.Collection;
import java.util.Iterator;

public interface IQuery<V,E> {
    /**
     * Executes the query and returns the matching edges.
     * @return
     */
    Iterator<IEdge<V, E>> edges();

    /**
     * Executes the query and returns the matching edges till the max limit
     * @param limit max number of vertices
     * @return
     */
    Iterator<IEdge<V, E>> edges(long limit);

    /**
     * Executes the query and returns the matching edges from given offset till the max limit
     * @param offset starting offset
     * @param limit max number of vertices
     * @return
     */
    Iterator<IEdge<V, E>> edges(long offset, long limit);

    /**
     * Executes the query and returns the matching vertices.
     * @return
     */
    Iterator<IVertex<V, E>> vertices();

    /**
     * Executes the query and returns the matching vertices from given offset till the max limit
     * @param limit max number of vertices
     * @return
     */
    Iterator<IVertex<V, E>> vertices(long limit);

    /**
     * Executes the query and returns the matching vertices from given offset till the max limit
     * @param offset starting offset
     * @param limit max number of vertices
     * @return
     */
    Iterator<IVertex<V, E>> vertices(long offset, long limit);

    /**
     * Executes the query and returns IDs of matching vertices.
     * @return
     */
    Iterator<Object> vertexIds();

    /**
     * Executes the query and returns IDs of the matching vertices from given offset till the max limit
     * @param limit max number of vertices
     * @return
     */
    Iterator<Object> vertexIds(long limit);

    /**
     * Executes the query and returns IDs of the matching vertices from given offset till the max limit
     * @param offset starting offset
     * @param limit max number of vertices
     * @return
     */
    Iterator<Object> vertexIds(long offset, long limit);

    /**
     * Adds a predicate that the returned vertices must have the specified
     * property and that one of the values of the property must be the
     * given value.
     *
     * @param property
     * @param value
     * @return
     */
    IQuery<V, E> has(String property, Object value);

    /**
     * Adds a predicate that the returned vertices must have the specified
     * property and that one of the value of the property must be in
     * the specified list of values.
     *
     * @param property
     * @param values
     * @return
     */
    IQuery<V, E> in(String property, Collection<?> values);

    /**
     *
     * @param property
     * @param value
     * @return
     */
    IQuery<V, E> lt(String property, String value);

    /**
     *
     * @param property
     * @param value
     * @return
     */
    IQuery<V, E> lte(String property, String value);

    IQuery<V, E> gt(String property, String value);

    IQuery<V, E> gte(String property, String value);

    IQuery<V, E> eq(String property, String value);

    IQuery<V, E> neq(String property, String value);

    IQuery<V, E> isNull(String value);

    IQuery<V, E> notNull(String value);

    IQuery<V, E> startWith(String property, String value);

    IQuery<V, E> endWith(String property, String value);

    IQuery<V, E> notEndWith(String property, String value);

    IQuery<V, E> contains(String property, String value);

    IQuery<V, E> notContains(String property, String value);

}
