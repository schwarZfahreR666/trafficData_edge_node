package com.example.edge_node.neo4j;

import com.example.edge_node.config.Neo4jConfig;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Repository;

/**
 * Create by zhangran
 */
public class BaseSession {
    private Driver driver;
    private Session session;



    public BaseSession() {

        driver = GraphDatabase.driver( "bolt://" + Neo4jConfig.uri, AuthTokens.basic(Neo4jConfig.username, Neo4jConfig.password) );

        session = driver.session();
    }

    public Session getSession(){
        return session;
    }

    public void close(){
        session.close();
        driver.close();

    }
}
