package com.example.edge_node.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Create by zhangran
 */
@Data
public class Neo4jConfig {
    public static String uri = "47.95.159.86:7687";
    public static String username = "neo4j";
    public static String password = "06240118";
}
