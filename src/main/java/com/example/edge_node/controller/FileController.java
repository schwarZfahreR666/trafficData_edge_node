package com.example.edge_node.controller;

import com.example.edge_node.service.ContainerService;
import com.example.edge_node.service.FileService;
import com.example.edge_node.service.ImageService;
import com.example.edge_node.service.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


/**
 * Create by zhangran
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Autowired
    FileService fileService;
    @Autowired
    ImageService imageService;
    @Autowired
    ContainerService containerService;
    @Autowired
    MonitorService monitorService;
    @Value("${filepath}")
    String filepath;

    /**
     * 处理文件上传
     */
    @PostMapping(value = "/build")
    public String uploading(@RequestParam("files") MultipartFile[] files,@RequestParam("dockerfile") String content,@RequestParam("tag") String tag) throws Exception {
        fileService.saveDockerfile(content);
        fileService.saveFiles(files);
        imageService.buildImage(tag);
        containerService.create(tag,tag,null);
//        fileService.copyTarToContainer(tag,"/Users/zhangran/Documents/edge_node/files/test.tar","/code/");
        fileService.copyFileToContainer(tag,filepath + "test.txt","/code/");

        Thread.sleep(5000);
        containerService.containerStart(tag);

        String res = fileService.copyFromContainer(tag,"/code/res.txt");
        for (int i = 0; i < 20; i++) {
            containerService.containerStart(tag);
            while(!"exited".equals(monitorService.inspectContainer(tag).getState().getStatus())){
                System.out.println("wait!");
            }
            res += "\n" + fileService.copyFromContainer(tag,"/code/res.txt");
        }

        System.out.println(containerService.log(tag));
        containerService.delete(tag);
        imageService.delete(tag);
        return res;
    }

}
