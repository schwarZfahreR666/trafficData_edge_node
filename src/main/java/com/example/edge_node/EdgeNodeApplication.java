package com.example.edge_node;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class EdgeNodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(EdgeNodeApplication.class, args);
    }

}
