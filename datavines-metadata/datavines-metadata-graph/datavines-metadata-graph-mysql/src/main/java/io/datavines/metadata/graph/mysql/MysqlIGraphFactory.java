package io.datavines.metadata.graph.mysql;

import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.umlg.sqlg.structure.SqlgGraph;
import io.datavines.metadata.graph.api.IGraphFactory;

public class MysqlIGraphFactory implements IGraphFactory {

    @Override
    public Graph getGraph(Configuration configuration) {
        return SqlgGraph.open(configuration);
    }
}
