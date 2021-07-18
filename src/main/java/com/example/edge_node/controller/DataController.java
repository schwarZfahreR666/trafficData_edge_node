package com.example.edge_node.controller;

import com.example.edge_node.pojo.HostInfo;
import com.example.edge_node.pojo.SysMonitor;
import com.example.edge_node.service.ContainerService;
import com.example.edge_node.service.FileService;
import com.example.edge_node.service.ImageService;
import com.example.edge_node.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.SocketException;
import java.util.Objects;

/**
 * Create by zhangran
 */
@RestController
@RequestMapping("/data")
public class DataController {
    @Autowired
    MonitorService monitorService;
    @Autowired
    ContainerService containerService;
    @Autowired
    FileService fileService;
    @Autowired
    ImageService imageService;
    @GetMapping("/hostInfo")
    public HostInfo getHostInfo(){
        HostInfo hostInfo = monitorService.getInfo();
        return hostInfo;
    }

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

    @GetMapping("/ctnlog")
    public String getCtnLog(@RequestParam String id){
        String log = "";

        try {
             log = containerService.log(id);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return log;
    }

    @GetMapping("/ctnres")
    public String getCtnres(@RequestParam String id) {
        String res = "";

        while("running".equals(monitorService.inspectContainer(id).getState().getStatus())){

        }
        try {
            String workDir = Objects.requireNonNull(imageService.info(monitorService.inspectContainer(id).getImageId()).getContainerConfig()).getWorkingDir();
            res = fileService.copyFromContainer(id,workDir + "/res.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }
}
