package com.example.edge_node.controller;

import com.example.edge_node.mapper.ImageMapper;
import com.example.edge_node.pojo.HostInfo;
import com.example.edge_node.pojo.Image;
import com.example.edge_node.pojo.SysMonitor;
import com.example.edge_node.service.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.SocketException;
import java.util.List;

/**
 * Create by zhangran
 */
@RestController
@RequestMapping("/rest")
@Slf4j
public class RestApiController {
    @Autowired
    MonitorService monitorService;
    @Autowired
    ImageMapper imageMapper;
    @GetMapping("/sysInfo")
    public SysMonitor getSysInfo(){
        SysMonitor sysMonitor = new SysMonitor();
        try {
            sysMonitor = monitorService.SystemMonitor();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return sysMonitor;
    }

    @GetMapping("/hostInfo")
    public HostInfo getHostInfo(){
        HostInfo hostInfo = monitorService.getInfo();
        return hostInfo;
    }

    @GetMapping("/tasks")
    public List<Image> getTasks(){
        return imageMapper.getImages();
    }

    @GetMapping("/health")
    public String gethealth(){
        String health = "获取节点健康状况失败。";
        log.info("获取节点健康度");
        try {
             health = monitorService.evalHealth();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return health;
    }
}
