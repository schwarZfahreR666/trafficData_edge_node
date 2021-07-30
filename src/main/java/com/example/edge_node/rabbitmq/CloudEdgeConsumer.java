package com.example.edge_node.rabbitmq;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Create by zhangran
 */
@Service
@Slf4j
public class CloudEdgeConsumer {
    @Autowired
    EdgeCloudProducer edgeCloudProducer;
    @RabbitListener(queues = {"cloud-edge-queue"})
    public void reviceMessage(String message){
        JSONObject jsonObject =  JSONObject.parseObject(message);
        log.info(">>>>>>>>>> record =" + jsonObject.toJSONString());
        Status status = new Status(0,"启动任务成功");
        String exchangeName = "edge-cloud";
        String routingKey = "";
        edgeCloudProducer.send(exchangeName,routingKey,status);
    }
}
