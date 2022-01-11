package com.example.edge_node.service;

import com.example.edge_node.cluster.dto.Message;
import com.example.edge_node.cluster.slave.NettyClient;
import com.example.edge_node.config.MasterCondition;
import com.example.edge_node.config.SlaveCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.net.SocketException;
import java.util.Random;

/**
 * Create by zhangran
 */
//@Conditional(SlaveCondition.class)
@Service
@Slf4j
public class SlaveService {
    @Autowired
    NettyClient nettyClient;
    @Autowired
    MonitorService monitorService;
    @Value("${server.slave_id}")
    private String slaveId;

    public void sendHealth(){

//            Message message = Message.builder()
//                                    .requestId(String.valueOf(new Random().nextInt()))
////                                    .healthScore(monitorService.getHealthScore())
//                                    .healthScore(new Random().nextDouble() * 100)
//                                    .build();
        Message message = null;
        try {
            message = Message.builder()
                    .requestId(slaveId)
                    .healthScore(monitorService.getHealthScore())
                    .build();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        nettyClient.sendMessage(message);

    }
}
