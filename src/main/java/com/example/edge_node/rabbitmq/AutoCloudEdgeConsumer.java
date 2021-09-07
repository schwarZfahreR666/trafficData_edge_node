package com.example.edge_node.rabbitmq;

import com.alibaba.fastjson.JSONObject;
import com.example.edge_node.service.TaskService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
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
public class AutoCloudEdgeConsumer {
    @Autowired
    EdgeCloudProducer edgeCloudProducer;
    @Autowired
    TaskService taskService;
    @RabbitListener(queues = {"auto-cloud-edge-queue"})
    public void reviceMessage(String message, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel){
        JSONObject jsonObject =  JSONObject.parseObject(message);
        log.info(">>>>>>>>>> record =" + jsonObject.toJSONString());
        String taskName = jsonObject.getString("task_name");
        String input = "";
        String nodeName = jsonObject.getString("node_name");
        String res = "no";
        Status status = new Status(-1,"启动任务失败",taskName);
        if("BUAA".equals(nodeName)){
            String ans = taskService.startTask(taskName,input,res);
            status = new Status(0,"启动任务成功",taskName);
        }
        else{
            try {
                channel.basicReject(deliveryTag,true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        String exchangeName = "auto-edge-cloud";
        String routingKey = "";
        edgeCloudProducer.send(exchangeName,routingKey,status);
    }
}
