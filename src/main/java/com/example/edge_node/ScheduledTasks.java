package com.example.edge_node;

import com.example.edge_node.service.RegisterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Create by zhangran
 */
@Component
@Slf4j
public class ScheduledTasks {
    @Autowired
    RegisterService registerService;
    @Scheduled(fixedRate = 60000 * 5)
    public void register(){
        registerService.registeAllTask();
        log.info("于{}更新image注册信息",new SimpleDateFormat("HH:mm:ss").format(new Date()));
    }
}
