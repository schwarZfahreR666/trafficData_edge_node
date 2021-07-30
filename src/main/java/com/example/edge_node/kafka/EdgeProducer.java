package com.example.edge_node.kafka;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Create by zhangran
 */
//@Component
@Slf4j
public class EdgeProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Value("${spring.kafka.producer.topic}")
    private String topic;

    /**
     * 发送数据
     * @param data
     */
    public void send(Object data){
        kafkaTemplate.send(topic, JSON.toJSONString(data));
        log.info("kafka发送数据:" + data);
    }
}
