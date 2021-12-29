package com.example.edge_node.rabbitmq;

import com.alibaba.fastjson.JSONObject;
import com.example.edge_node.config.ConstantValue;
import com.example.edge_node.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    @Autowired
    TaskService taskService;
    @RabbitListener(queues = {"cloud-edge-queue-" + ConstantValue.NODE_NAME})
    public void reviceMessage(String message){
        JSONObject jsonObject =  JSONObject.parseObject(message);
        log.info(">>>>>>>>>> record =" + jsonObject.toJSONString());
        String taskName = jsonObject.getString("name");
        String input = jsonObject.getString("input");
        String time = jsonObject.getString("time");
        String res = jsonObject.getString("res");
        String ans = taskService.startTask(taskName,input,res);
        Status status = new Status(0,"启动任务成功",ans);
        String exchangeName = "edge-cloud-" + ConstantValue.NODE_NAME;
        String routingKey = "";
        edgeCloudProducer.send(exchangeName,routingKey,status);
    }
}
