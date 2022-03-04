package io.simfroce.bytezard.metadata.graph.neo4j;

import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.structure.Graph;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

import com.steelbridgelabs.oss.neo4j.structure.Neo4JElementIdProvider;
import com.steelbridgelabs.oss.neo4j.structure.Neo4JGraph;
import com.steelbridgelabs.oss.neo4j.structure.partitions.AnyLabelReadPartition;
import com.steelbridgelabs.oss.neo4j.structure.providers.Neo4JNativeElementIdProvider;

import io.simforce.bytezard.metadata.graph.api.IGraphFactory;

public class Neo4JBoltIGraphFactory implements IGraphFactory {

    @Override
    public Graph getGraph(Configuration configuration) {
        Neo4JElementIdProvider<?> provider = new Neo4JNativeElementIdProvider();
        Driver driver = GraphDatabase.driver(
                configuration.getString("uri"),
                AuthTokens.basic(configuration.getString("username"),
                        configuration.getString("password")));
        return new Neo4JGraph(new AnyLabelReadPartition("bytezard"), new String[]{"bytezard"}, driver, provider, provider);
    }
}
