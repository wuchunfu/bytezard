package io.simforce.bytezard.metadata.graph.mysql;

import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.umlg.sqlg.structure.SqlgGraph;

import io.simforce.bytezard.metadata.graph.api.GraphFactory;

public class MysqlGraphFactory implements GraphFactory {

    @Override
    public Graph getGraph(Configuration configuration) {
        return SqlgGraph.open(configuration);
    }
}
