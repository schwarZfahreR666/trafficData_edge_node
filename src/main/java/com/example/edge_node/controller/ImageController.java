package com.example.edge_node.controller;

import com.example.edge_node.mapper.ImageMapper;
import com.example.edge_node.pojo.Image;
import com.example.edge_node.service.ContainerService;
import com.example.edge_node.service.FileService;
import com.example.edge_node.service.ImageService;
import com.example.edge_node.service.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


/**
 * Create by zhangran
 */

@Controller
@RequestMapping("/image")
@Slf4j
public class ImageController {

    @Autowired
    FileService fileService;
    @Autowired
    ImageService imageService;
    @Autowired
    ContainerService containerService;
    @Autowired
    MonitorService monitorService;
    @Autowired
    ImageMapper imageMapper;
    @Value("${filepath}")
    String filepath;

    /**
     * 编译镜像
     */
    @PostMapping(value = "/build")
    public String uploading(@RequestParam("files") MultipartFile[] files,@RequestParam("dockerfile") String content,@RequestParam("tag") String tag) throws Exception {
        fileService.saveDockerfile(content);
        fileService.saveFiles(files);
        imageService.buildImage(tag);
        String id = imageService.info(tag).getId();
        imageMapper.saveImage(new Image(tag,id));
        log.info("镜像编译成功");

        return "redirect:/imageslist";
    }
    /**
    * 删除镜像
    */
    @RequestMapping("/delimage")
    public String delImage(@RequestParam String id){
        imageService.delete(id);
        imageMapper.delImage(id);
        log.info("镜像删除成功");
        return "redirect:/imageslist";
    }

    /**
     * 创建容器
     * */
    @RequestMapping("/createctn")
    public String createCtn(@RequestParam String image){
        String uuid = UUID.randomUUID().toString();

        containerService.create(image,uuid,null);

        return "redirect:/imageslist";
    }



}
