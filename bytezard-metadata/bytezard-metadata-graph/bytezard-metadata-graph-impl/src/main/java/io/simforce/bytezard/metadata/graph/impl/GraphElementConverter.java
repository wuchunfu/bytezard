package io.simforce.bytezard.metadata.graph.impl;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import io.simforce.bytezard.metadata.graph.api.EdgeDirection;

public class GraphElementConverter {

    private GraphElementConverter () {}

    public static BytezardEdge toBytezardEdge(BytezardGraph graph, Edge source) {

        if (source == null) {
            return null;
        }
        return new BytezardEdge(graph, source);
    }

    public static BytezardVertex toBytezardVertex(BytezardGraph graph, Vertex source) {

        if (source == null) {
            return null;
        }
        return new BytezardVertex(graph, source);
    }

    /**
     * Retrieves the Janus direction corresponding to the given EdgeDirection.
     *
     * @param dir
     * @return
     */
    public static Direction toDirection(EdgeDirection dir) {

        switch(dir) {
            case IN:
                return Direction.IN;
            case OUT:
                return Direction.OUT;
            case BOTH:
                return Direction.BOTH;
            default:
                throw new RuntimeException("Unrecognized direction: " + dir);
        }
    }
}
