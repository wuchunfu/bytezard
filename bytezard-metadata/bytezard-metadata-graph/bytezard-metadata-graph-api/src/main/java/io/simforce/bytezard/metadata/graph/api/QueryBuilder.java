package io.simforce.bytezard.metadata.graph.api;

import static org.apache.tinkerpop.gremlin.process.traversal.P.*;
import static org.apache.tinkerpop.gremlin.process.traversal.TextP.*;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * @author zixi0825
 */
public class QueryBuilder<T1,T2> {

    private final GraphTraversal<T1, T2> graphTraversal;

    public QueryBuilder(GraphTraversal<T1, T2> graphTraversal) {
        this.graphTraversal = graphTraversal;
    }

    public GraphTraversal<T1, T2> searchTypeFilter(String... typeNames) {
       return this.graphTraversal.has("typeName", within(typeNames));
    }

    public GraphTraversal<T1, T2> searchClassificationFilter(String... classificationNames){
        return this.graphTraversal.or(has("__traitNames", within(classificationNames)), has("__propagatedTraitNames", within(classificationNames)));
    }

    public GraphTraversal<T1, T2> searchStateFilter(String state) {
        return this.graphTraversal.has("__state", state);
    }

    public GraphTraversal<T1, T2> lt(String property, String value) {
        return this.graphTraversal.has(property, P.lt(value));
    }

    public GraphTraversal<T1, T2> lte(String property, String value) {
        return this.graphTraversal.has(property, P.lte(value));
    }

    public GraphTraversal<T1, T2> gt(String property, String value) {
        return this.graphTraversal.has(property, P.gt(value));
    }

    public GraphTraversal<T1, T2> gte(String property, String value) {
        return this.graphTraversal.has(property, P.gte(value));
    }

    public GraphTraversal<T1, T2> eq(String property, String value) {
        return this.graphTraversal.has(property, P.eq(value));
    }

    public GraphTraversal<T1, T2> neq(String property, String value) {
        return this.graphTraversal.has(property, P.neq(value));
    }

    public GraphTraversal<T1, T2> isNull(String value) {
        return this.graphTraversal.hasNot(value);
    }

    public GraphTraversal<T1, T2> notNull(String value) {
        return this.graphTraversal.has(value);
    }

    public GraphTraversal<T1, T2> startWith(String property, String value) {
        return this.graphTraversal.has(property,startingWith(value));
    }

    public GraphTraversal<T1, T2> endWith(String property, String value) {
        return this.graphTraversal.has(property,endingWith(value));
    }

    public GraphTraversal<T1, T2> notEndWith(String property, String value) {
        return this.graphTraversal.has(property,notEndingWith(value));
    }

    public GraphTraversal<T1, T2> contains(String property, String value) {
        return this.graphTraversal.has(property,containing(value));
    }

    public GraphTraversal<T1, T2> notContains(String property, String value) {
        return this.graphTraversal.has(property,notContaining(value));
    }

    public GraphTraversal<T1, T2> build() {
        return this.graphTraversal;
    }
}
