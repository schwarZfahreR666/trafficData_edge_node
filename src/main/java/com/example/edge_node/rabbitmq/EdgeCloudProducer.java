package com.example.edge_node.rabbitmq;

import com.alibaba.fastjson.JSONObject;
import com.example.edge_node.config.MasterCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * Create by zhangran
 */
@Conditional(MasterCondition.class)
@Component
@Slf4j
public class EdgeCloudProducer {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(String exchangeName,String routingKey,Object body){
        String jsonBody =  JSONObject.toJSONString(body);
        rabbitTemplate.convertAndSend(exchangeName,routingKey,jsonBody);
        log.info("发送数据：" + jsonBody);
    }
}
