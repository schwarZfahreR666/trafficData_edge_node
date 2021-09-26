package com.example.edge_node.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Create by zhangran
 */
@Configuration
public class RabbitConfig {
    //1.创建交换机
    @Bean
    public FanoutExchange edge_sendExchange(){
        return new FanoutExchange("edge-send",true,false);
    }
    @Bean
    public FanoutExchange cloud_sendExchange(){
        return new FanoutExchange("cloud-send",true,false);
    }

    @Bean
    public FanoutExchange edge_cloudExchange(){
        return new FanoutExchange("edge-cloud",true,false);
    }
    @Bean
    public FanoutExchange cloud_edgeExchange(){
        return new FanoutExchange("cloud-edge",true,false);
    }

    @Bean
    public FanoutExchange auto_edge_cloudExchange(){
        return new FanoutExchange("auto-edge-cloud",true,false);
    }
    @Bean
    public FanoutExchange auto_cloud_edgeExchange(){
        return new FanoutExchange("auto-cloud-edge",true,false);
    }
    //2.创建队列
    @Bean
    public Queue edge_sendQueue(){
        return new Queue("edge-send-queue");
    }
    @Bean
    public Queue cloud_sendQueue(){
        return new Queue("cloud-send-queue");
    }

    @Bean
    public Queue edge_cloudQueue(){
        return new Queue("edge-cloud-queue");
    }
    @Bean
    public Queue cloud_edgeQueue(){
        return new Queue("cloud-edge-queue");
    }

    @Bean
    public Queue auto_edge_cloudQueue(){
        return new Queue("auto-edge-cloud-queue");
    }
    @Bean
    public Queue auto_cloud_edgeQueue(){
        return new Queue("auto-cloud-edge-queue");
    }
    //3.绑定关系
    @Bean
    public Binding edge_sendBinding(){
        return BindingBuilder.bind(edge_sendQueue()).to(edge_sendExchange());
    }
    @Bean
    public Binding cloud_sendBinding(){
        return BindingBuilder.bind(cloud_sendQueue()).to(cloud_sendExchange());
    }

    @Bean
    public Binding edge_cloudBinding(){
        return BindingBuilder.bind(edge_cloudQueue()).to(edge_cloudExchange());
    }
    @Bean
    public Binding cloud_edgeBinding(){
        return BindingBuilder.bind(cloud_edgeQueue()).to(cloud_edgeExchange());
    }

    @Bean
    public Binding auto_edge_cloudBinding(){
        return BindingBuilder.bind(auto_edge_cloudQueue()).to(auto_edge_cloudExchange());
    }
    @Bean
    public Binding auto_cloud_edgeBinding(){
        return BindingBuilder.bind(auto_cloud_edgeQueue()).to(auto_cloud_edgeExchange());
    }


}
