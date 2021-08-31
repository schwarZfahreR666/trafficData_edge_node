package com.example.edge_node.rabbitmq;

import com.alibaba.fastjson.JSONObject;
import com.example.edge_node.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public void reviceMessage(String message){
        JSONObject jsonObject =  JSONObject.parseObject(message);
        log.info(">>>>>>>>>> record =" + jsonObject.toJSONString());
        String taskName = jsonObject.getString("task_name");
        String input = "";
        String nodeName = jsonObject.getString("node_name");
        String res = "no";
        if("BUAA".equals(nodeName)){
                    String ans = taskService.startTask(taskName,input,res);
        }

        Status status = new Status(0,"启动任务成功",taskName);
        String exchangeName = "auto-edge-cloud";
        String routingKey = "";
        edgeCloudProducer.send(exchangeName,routingKey,status);
    }
}
