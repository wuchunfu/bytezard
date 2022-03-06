package io.datavines.metadata.graph.api;

import java.util.Collection;

/**
 * @author zixi0825
 */
public interface IQueryBuilder<V,E> {
    /**
     * Adds a predicate that the returned vertices must have the specified
     * property and that one of the values of the property must be the
     * given value.
     *
     * @param property
     * @param value
     * @return
     */
    IQueryBuilder<V, E> has(String property, Object value);

    /**
     * Adds a predicate that the returned vertices must have the specified
     * property and that one of the value of the property must be in
     * the specified list of values.
     *
     * @param property
     * @param values
     * @return
     */
    IQueryBuilder<V, E> in(String property, Collection<?> values);

    IQueryBuilder<V, E> lt(String property, String value);

    IQueryBuilder<V, E> lte(String property, String value);

    IQueryBuilder<V, E> gt(String property, String value);

    IQueryBuilder<V, E> gte(String property, String value);

    IQueryBuilder<V, E> eq(String property, String value);

    IQueryBuilder<V, E> neq(String property, String value);

    IQueryBuilder<V, E> isNull(String value);

    IQueryBuilder<V, E> notNull(String value);

    IQueryBuilder<V, E> startWith(String property, String value);

    IQueryBuilder<V, E> endWith(String property, String value);

    IQueryBuilder<V, E> notEndWith(String property, String value);

    IQueryBuilder<V, E> contains(String property, String value);

    IQueryBuilder<V, E> notContains(String property, String value);
}
