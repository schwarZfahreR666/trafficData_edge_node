package com.example.edge_node.rabbitmq;

import com.alibaba.fastjson.JSONObject;
import com.example.edge_node.controller.RestApiController;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Create by zhangran
 */
@Service
@Slf4j
public class RestApiMQ {
    @Autowired
    RestApiController restApiController;
    @Autowired
    EdgeCloudProducer edgeCloudProducer;
    @RabbitListener(queues = {"cloud-send-queue"})
    public void reviceMessage(String message, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel){
        JSONObject jsonObject =  JSONObject.parseObject(message);
        log.info(">>>>>>>>>> record =" + jsonObject.toJSONString());
        String infoName = jsonObject.getString("info_name");

        String nodeName = jsonObject.getString("node_name");
        if("BUAA".equals(nodeName)){
            String exchangeName = "edge-send";
            String routingKey = "";
            InfoContent res = null;
            switch(infoName){
                case "sysInfo":
                    res = new InfoContent("sysInfo",restApiController.getSysInfo());

                    break;

                case "hostInfo":
                    res = new InfoContent("hostInfo",restApiController.getHostInfo());

                    break;
                case "tasks":
                    res = new InfoContent("tasks",restApiController.getTasks());

                    break;

                case "health":
                    res = new InfoContent("health",restApiController.gethealth());

                    break;
            }
            edgeCloudProducer.send(exchangeName,routingKey,res);
        }
        else{
            try {
                channel.basicReject(deliveryTag,true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
