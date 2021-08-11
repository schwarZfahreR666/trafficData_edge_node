package com.example.edge_node.controller;

import com.example.edge_node.service.ContainerService;
import com.example.edge_node.service.FileService;
import com.example.edge_node.service.ImageService;
import com.example.edge_node.service.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Objects;

/**
 * Create by zhangran
 */
@Controller
@RequestMapping("/ctn")
@Slf4j
public class ContainerController {
    @Autowired
    ContainerService containerService;
    @Autowired
    FileService fileService;
    @Autowired
    MonitorService monitorService;
    @Autowired
    ImageService imageService;
    /**
     * 删除容器
     */
    @RequestMapping("/delctn")
    public String delCtn(@RequestParam String id){
        containerService.delete(id);
        log.info("容器删除成功");
        return "redirect:/ctnlist";
    }

    /*
    * 停止容器
    * */
    @RequestMapping("/stopctn")
    public String stopCtn(@RequestParam String id){
        containerService.stop(id);
        log.info("容器停止成功");
        return "redirect:/ctnlist";
    }

    /**
     * 启动容器
     */
    @PostMapping("/startctn")
    public String stratCtn(@RequestParam String id,@RequestParam String input) throws InterruptedException, IOException {
        if(!"".equals(input)){
            log.info("存储输入文件");
            fileService.saveInput(input);
            log.info("添加输入文件到容器");
            String workDir = Objects.requireNonNull(imageService.info(monitorService.inspectContainer(id).getImageId()).getContainerConfig()).getWorkingDir();
            fileService.copyFileToContainer(id,fileService.getFilepath() + "input.txt",  workDir + "/");
        }


        containerService.containerStart(id);

        log.info("容器启动成功");
        return "redirect:/ctnlist";
    }


}
