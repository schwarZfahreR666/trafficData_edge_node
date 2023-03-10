package com.example.edge_node.neo4j;

import com.example.edge_node.config.Neo4jConfig;
import org.neo4j.driver.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Create by zhangran
 */

public enum BaseService {
    DATASOURCE;
    private Driver driver;
    private Session session;



    BaseService() {
//        Config config = Config.builder().
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
