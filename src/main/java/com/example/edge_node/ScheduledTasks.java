package com.example.edge_node;

import com.example.edge_node.config.MasterCondition;
import com.example.edge_node.service.OfflineService;
import com.example.edge_node.service.RegisterService;
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
@Conditional(MasterCondition.class)
@Component
@Slf4j
public class ScheduledTasks {
    @Autowired
    RegisterService registerService;
    @Autowired
    OfflineService offlineService;
    @Value("${spring.rabbitmq.host}")
    String ipPath;
    @Scheduled(fixedRate = 60000 * 5)
    public void register(){
        registerService.registeAllTask();
        log.info("于{}更新image注册信息",new SimpleDateFormat("HH:mm:ss").format(new Date()));
    }

    @Scheduled(fixedRate = 60000 * 5)
    public void offline(){
        if(!NetUtils.isConnect(ipPath)){
            offlineService.scheduleTask();
            log.info("于{}检查离线任务列表",new SimpleDateFormat("HH:mm:ss").format(new Date()));
        }

    }

}
