package com.example.edge_node.kafka;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Create by zhangran
 */
//@Component
@Slf4j
public class EdgeConsumer {
//    @Autowired
//    EdgeProducer edgeProducer;
//    @KafkaListener(topics = {"cloud-edge"})
    public void consumer(ConsumerRecord<?,?> consumerRecord){
        //判断是否为null
        Optional<?> kafkaMessage = Optional.ofNullable(consumerRecord.value());
        log.info(">>>>>>>>>> record =" + kafkaMessage);
        if(kafkaMessage.isPresent()){
            //得到Optional实例中的值
            Object message = kafkaMessage.get();
            JSONObject jsonObject = JSONObject.parseObject(message.toString());

            Status status = new Status(0,"启动任务成功");
//            edgeProducer.send(status);
        }
    }
}
