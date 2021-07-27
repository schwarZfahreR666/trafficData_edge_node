package com.example.edge_node.controller;

import com.example.edge_node.mapper.ImageMapper;
import com.example.edge_node.pojo.Image;
import com.example.edge_node.pojo.SysMonitor;
import com.example.edge_node.service.MonitorService;
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

    @GetMapping("/tasks")
    public List<Image> getTasks(){
        return imageMapper.getImages();
    }
}
