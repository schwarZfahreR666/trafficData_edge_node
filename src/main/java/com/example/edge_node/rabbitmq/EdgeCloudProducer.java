package com.example.edge_node.rabbitmq;

import com.alibaba.fastjson.JSONObject;
import com.example.edge_node.config.MasterCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
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
        rabbitTemplate.convertAndSend(exchangeName,routingKey,jsonBody, message -> {
            // 设置消息的过期时间，单位为毫秒
            message.getMessageProperties().setExpiration("30000");
            return message;
        });
        log.info("发送数据：" + jsonBody);
    }
}
