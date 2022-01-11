package com.example.edge_node;

import com.example.edge_node.cluster.ds.HealthMap;
import com.example.edge_node.cluster.dto.Message;
import com.example.edge_node.cluster.dto.SlaveInfo;
import com.example.edge_node.cluster.master.NettyServer;
import com.example.edge_node.service.MasterService;
import com.example.edge_node.service.SlaveService;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.SimpleDateFormat;
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Create by zhangran
 */
@SpringBootTest
public class ClusterTest {
    @Autowired
    SlaveService slaveService;
    @Autowired
    MasterService masterService;
    @Autowired
    NettyServer nettyServer;

    @Test
    public void serverRun(){
        nettyServer.run();
    }
    @Test
    public void testCluster() throws InterruptedException {
        new Thread(()->
            nettyServer.run()
        ).start();
        Thread.sleep(10000);
        new Thread(()->
                slaveService.sendHealth()
        ).start();
        Thread.sleep(2000);
        new Thread(()->
                slaveService.sendHealth()
        ).start();
        Thread.sleep(2000);
        new Thread(()->
                slaveService.sendHealth()
        ).start();
        Thread.sleep(2000);
        new Thread(()->
                slaveService.sendHealth()
        ).start();
        Thread.sleep(2000);
        new Thread(()->
                slaveService.sendHealth()
        ).start();
        Thread.sleep(10000);
        Message message = Message.builder().taskName("road_info").build();
//        masterService.sendMessage2slave("123",message);
        masterService.sendMessage2health(message);
    }

    @Test
    public void testHealthMap() throws InterruptedException {
        HealthMapTest map = new HealthMapTest();
        new Thread(()->System.out.println(map.getHealthySlave())).start();
        new Thread(()-> map.put("123","92.1",92.1)).start();
        new Thread(()-> map.put("123","90.6",90.6)).start();
        new Thread(()-> map.put("123","93.2",93.2)).start();
        new Thread(()-> map.put("123","80.6",80.6)).start();
        new Thread(()-> map.put("1234","93.2",93.2)).start();
        new Thread(()-> map.put("1233","90.6",90.6)).start();
        new Thread(()->System.out.println(map.getHealthySlave())).start();

    }
}
