package io.datavines.metadata.graph.impl;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import io.datavines.metadata.graph.api.EdgeDirection;

public class GraphElementConverter {

    private GraphElementConverter () {}

    public static DataVinesEdge toDataVinesEdge(DataVinesGraph graph, Edge source) {

        if (source == null) {
            return null;
        }
        return new DataVinesEdge(graph, source);
    }

    public static DataVinesVertex toDataVinesVertex(DataVinesGraph graph, Vertex source) {

        if (source == null) {
            return null;
        }
        return new DataVinesVertex(graph, source);
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
