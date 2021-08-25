package com.example.edge_node.neo4j;

import com.example.edge_node.config.Neo4jConfig;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Create by zhangran
 */

public enum BaseService {
    DATASOURCE;
    private Driver driver;
    private Session session;



    BaseService() {

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
