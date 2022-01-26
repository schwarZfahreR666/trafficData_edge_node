package com.example.edge_node;

import com.example.edge_node.config.MasterCondition;
import com.example.edge_node.config.SlaveCondition;
import com.example.edge_node.service.OfflineService;
import com.example.edge_node.service.RegisterService;
import com.example.edge_node.service.SlaveService;
import com.example.edge_node.utils.NetUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Create by zhangran
 */
@Conditional(SlaveCondition.class)
@Component
@Slf4j
public class SlaveScheduledTasks {
    @Autowired
    SlaveService slaveService;
    @Scheduled(fixedRate = 30000)
    public void register(){
        new Thread(()->
                slaveService.sendHealth()
        ).start();
        log.info("于{}向master发送健康信息",new SimpleDateFormat("HH:mm:ss").format(new Date()));
    }

}
