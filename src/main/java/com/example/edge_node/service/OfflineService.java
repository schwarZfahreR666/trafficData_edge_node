package com.example.edge_node.service;

import com.example.edge_node.pojo.TaskTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Create by zhangran
 */
@Service
@Slf4j
public class OfflineService {
    private static Map<String, TaskTime> TASK_MAP = new ConcurrentHashMap<String, TaskTime>();
    @Autowired
    TaskService taskService;

    public void putTask(String name,int freq){
        long currentTime = System.currentTimeMillis();
        TaskTime taskTime = new TaskTime(freq,currentTime);
        TASK_MAP.put(name,taskTime);
    }

    public void scheduleTask(){
        for(Map.Entry<String,TaskTime> entry : TASK_MAP.entrySet()) {
            String taskName = entry.getKey();
            int freq = entry.getValue().getFreq();
            long lastTime = entry.getValue().getLastTime();
            if(System.currentTimeMillis() - lastTime >= freq * 60L){
                String ans = taskService.startTask(taskName,"","no");
                log.info("offline exec " + taskName);
            }

        }
    }
}
