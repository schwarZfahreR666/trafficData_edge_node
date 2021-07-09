package com.example.edge_node.controller;

import com.example.edge_node.service.ContainerService;
import com.example.edge_node.service.FileService;
import com.example.edge_node.service.ImageService;
import com.example.edge_node.utils.ZipUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    /**
     * 处理文件上传
     */
    @PostMapping(value = "/build")
    public String uploading(@RequestParam("files") MultipartFile[] files,@RequestParam("dockerfile") String content,@RequestParam("tag") String tag) throws Exception {
        fileService.saveDockerfile(content);
        System.out.println(content);
        fileService.saveFiles(files);
        imageService.buildImage(tag);
        containerService.create(tag,tag,null);
        fileService.copyTarToContainer(tag,"/Users/zhangran/Documents/edge_node/files/test.tar","/code/");
        containerService.containerStart(tag);


        String res = fileService.copyFromContainer(tag,"/code/res.txt");

        System.out.println(containerService.log(tag));
        containerService.delete(tag);
        return res;
    }

}
