package com.example.edge_node;

import com.example.edge_node.service.*;
import com.example.edge_node.utils.ZipUtils;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
class EdgeNodeApplicationTests {

    @Autowired
    DockerService dockerService;
    @Autowired
    ImageService imageService;
    @Autowired
    ContainerService containerService;
    @Autowired
    MonitorService monitorService;
    @Autowired
    FileService fileService;

    @Test
    void contextLoads() {
        List<Container> containers = dockerService.getDockerClient().listContainersCmd()
//                .withLabelFilter(testLabel) //容器类别的标识，是一个string，string的map
                .withShowAll(true)
                .exec();
        for (Container container : containers) {
            System.out.println(container);
        }

        List<Image> images = dockerService.getDockerClient().listImagesCmd()
                .withShowAll(true)
                .exec();

        for (Image image : images) {
            System.out.println(image);
        }
    }

    @Test
    void testImage(){
        for (Image image : imageService.list()) {
            InspectImageResponse inspectImageResponse = imageService.info(image.getId());
            System.out.println(inspectImageResponse);
        }


    }
    @Test
    void testContainer() throws IOException, InterruptedException {

        containerService.create("busybox","hello1","top");
//        fileService.copyTarToContainer("hello1","/Users/zhangran/Documents/edge_node/files/test.tar","/");
//        fileService.copyFileToContainer("hello1","/Users/zhangran/Documents/edge_node/files/requirements.txt","/code/");
        containerService.containerStart("hello1");
//        System.out.println(containerService.log("hello1"));

        System.out.println(monitorService.getStats("hello1"));
//        System.out.println(monitorService.getInfo());
//        System.out.println(monitorService.inspectContainer("hello1"));
//        System.out.println(monitorService.getInspect("hello1"));
//
//        System.out.println(fileService.copyFromContainer("hello1","/code/requirements.txt"));
        containerService.stop("hello1");
//        System.out.println(monitorService.getInspect("hello1"));
//        System.out.println(containerService.log("hello1"));
//
        containerService.delete("hello1");
    }
    @Test
    void buildImage() throws Exception {
        imageService.buildImage("road");
    }

    @Test
    void testUnZip() throws IOException {
        Path file = Paths.get("/Users/zhangran/Documents/edge_node/files/app.zip");
        Path targetPath = Paths.get("/Users/zhangran/Documents/edge_node/files");

        ZipUtils.unPacket(file,targetPath);
    }



}
