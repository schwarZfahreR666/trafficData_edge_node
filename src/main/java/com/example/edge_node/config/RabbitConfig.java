package com.example.edge_node.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * Create by zhangran
 */
@Conditional(MasterCondition.class)
@Configuration
public class RabbitConfig {
    @Value("${server.nodeName}")
    String nodeName;
    //1.创建交换机
    @Conditional(MasterCondition.class)
    @Bean
    public FanoutExchange edge_sendExchange(){
        return new FanoutExchange("edge-send-" + nodeName,true,false);
    }
    @Conditional(MasterCondition.class)
    @Bean
    public DirectExchange cloud_sendExchange(){
        return new DirectExchange("cloud-send",true,false);
    }

    @Conditional(MasterCondition.class)
    @Bean
    public FanoutExchange edge_cloudExchange(){
        return new FanoutExchange("edge-cloud-" + nodeName,true,false);
    }
    @Conditional(MasterCondition.class)
    @Bean
    public DirectExchange cloud_edgeExchange(){
        return new DirectExchange("cloud-edge",true,false);
    }

    @Conditional(MasterCondition.class)
    @Bean
    public FanoutExchange auto_edge_cloudExchange(){
        return new FanoutExchange("auto-edge-cloud-" + nodeName,true,false);
    }
    @Conditional(MasterCondition.class)
    @Bean
    public DirectExchange auto_cloud_edgeExchange(){
        return new DirectExchange("auto-cloud-edge",true,false);
    }
    //2.创建队列
    @Conditional(MasterCondition.class)
    @Bean
    public Queue edge_sendQueue(){
        return new Queue("edge-send-queue-" + nodeName);
    }
    @Conditional(MasterCondition.class)
    @Bean
    public Queue cloud_sendQueue(){
        return new Queue("cloud-send-queue-" + nodeName);
    }

    @Conditional(MasterCondition.class)
    @Bean
    public Queue edge_cloudQueue(){
        return new Queue("edge-cloud-queue-" + nodeName);
    }
    @Conditional(MasterCondition.class)
    @Bean
    public Queue cloud_edgeQueue(){
        return new Queue("cloud-edge-queue-" + nodeName);
    }

    @Conditional(MasterCondition.class)
    @Bean
    public Queue auto_edge_cloudQueue(){
        return new Queue("auto-edge-cloud-queue-" + nodeName);
    }
    @Conditional(MasterCondition.class)
    @Bean
    public Queue auto_cloud_edgeQueue(){
        return new Queue("auto-cloud-edge-queue-" + nodeName);
    }
    //3.绑定关系
    @Conditional(MasterCondition.class)
    @Bean
    public Binding edge_sendBinding(){
        return BindingBuilder.bind(edge_sendQueue()).to(edge_sendExchange());
    }
    @Conditional(MasterCondition.class)
    @Bean
    public Binding cloud_sendBinding(){
        return BindingBuilder.bind(cloud_sendQueue()).to(cloud_sendExchange()).with(nodeName);
    }

    @Conditional(MasterCondition.class)
    @Bean
    public Binding edge_cloudBinding(){
        return BindingBuilder.bind(edge_cloudQueue()).to(edge_cloudExchange());
    }
    @Conditional(MasterCondition.class)
    @Bean
    public Binding cloud_edgeBinding(){
        return BindingBuilder.bind(cloud_edgeQueue()).to(cloud_edgeExchange()).with(nodeName);
    }

    @Conditional(MasterCondition.class)
    @Bean
    public Binding auto_edge_cloudBinding(){
        return BindingBuilder.bind(auto_edge_cloudQueue()).to(auto_edge_cloudExchange());
    }
    @Conditional(MasterCondition.class)
    @Bean
    public Binding auto_cloud_edgeBinding(){
        return BindingBuilder.bind(auto_cloud_edgeQueue()).to(auto_cloud_edgeExchange()).with(nodeName);
    }


}
