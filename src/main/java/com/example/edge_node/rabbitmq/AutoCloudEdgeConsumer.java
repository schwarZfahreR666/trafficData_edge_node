package com.example.edge_node.rabbitmq;

import com.alibaba.fastjson.JSONObject;
import com.example.edge_node.cluster.dto.Message;
import com.example.edge_node.config.ConstantValue;
import com.example.edge_node.config.MasterCondition;
import com.example.edge_node.service.MasterService;
import com.example.edge_node.service.OfflineService;
import com.example.edge_node.service.TaskService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Create by zhangran
 */
@Conditional(MasterCondition.class)
@Service
@Slf4j
public class AutoCloudEdgeConsumer {
    @Autowired
    EdgeCloudProducer edgeCloudProducer;
    @Autowired
    TaskService taskService;
    @Autowired
    OfflineService offlineService;
    @Autowired
    MasterService masterService;
    @Value("${server.nodeName}")
    String NODE_NAME;
    @RabbitListener(queues = {"auto-cloud-edge-queue-" + ConstantValue.NODE_NAME})
    public void reviceMessage(String message, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel channel){
        JSONObject jsonObject =  JSONObject.parseObject(message);
        log.info(">>>>>>>>>> record =" + jsonObject.toJSONString());
        String taskName = jsonObject.getString("task_name");
        String input = "";
        String nodeName = jsonObject.getString("node_name");
        String res = "no";
        Status status = new Status(-1,"启动任务失败",taskName);
        if(NODE_NAME.equals(nodeName)){
            Message msg = Message.builder().taskName(taskName).build();
            if(!masterService.sendMessage2health(msg)){
                String ans = taskService.startTask(taskName,input,res);
                log.info("master运行任务" + taskName);
            }
            offlineService.putTask(taskName,5);
            status = new Status(0,"启动任务成功",taskName);
        }
        else{
            try {
                channel.basicReject(deliveryTag,true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        String exchangeName = "auto-edge-cloud-" + ConstantValue.NODE_NAME;
        String routingKey = "";
        edgeCloudProducer.send(exchangeName,routingKey,status);
    }
}
