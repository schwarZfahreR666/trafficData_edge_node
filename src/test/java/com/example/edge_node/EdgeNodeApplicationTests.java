package com.example.edge_node;

import com.example.edge_node.kafka.EdgeProducer;
import com.example.edge_node.mapper.ImageMapper;
import com.example.edge_node.service.*;
import com.example.edge_node.utils.ZipUtils;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
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
    @Autowired
    ImageMapper imageMapper;


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
            System.out.println(image);
            InspectImageResponse inspectImageResponse = imageService.info(image.getId());

            System.out.println(inspectImageResponse.getContainerConfig().getWorkingDir());
        }
    }

    @Test
    void testContainers(){
        for (Container container : containerService.list()) {

            System.out.println(imageService.info(monitorService.inspectContainer(container.getId()).getImageId()).getContainerConfig().getWorkingDir());
        }
    }
    @Test
    void testContainer() throws IOException, InterruptedException {

        containerService.create("road","hello1",null);
//        fileService.copyTarToContainer("hello1","/Users/zhangran/Documents/edge_node/files/test.tar","/");
        fileService.copyFileToContainer("hello1","/Users/zhangran/Documents/edge_node/files/test.txt","/code/");
        containerService.containerStart("hello1");


//        System.out.println(monitorService.getStats("hello1"));
//        System.out.println(monitorService.getInfo());
        System.out.println(monitorService.inspectContainer("hello1"));
//        System.out.println(monitorService.getInspect("hello1"));
        while(!"exited".equals(monitorService.inspectContainer("hello1").getState().getStatus())){
            System.out.println("wait!");
        }
//
        System.out.println(fileService.copyFromContainer("hello1","/code/res"));
//        containerService.stop("hello1");
        containerService.stop("hello1");
//        System.out.println(monitorService.inspectContainer("hello1"));
//        System.out.println(monitorService.getInspect("hello1"));
        System.out.println(containerService.log("hello1"));
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
    @Test
    void testSystemMonitor(){
        try {
            System.out.println(monitorService.SystemMonitor());
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testImageMapper(){
        com.example.edge_node.pojo.Image image = new com.example.edge_node.pojo.Image();
        image.setId("1111111112222");
        image.setName("1111");
        imageMapper.saveImage(image);
        System.out.println(imageMapper.getImageByName("1111"));
//        imageMapper.delImage(image);
        System.out.println(imageMapper.getImageByName("1111"));
        System.out.println(imageMapper.getImages());
    }


    @Test
    void eval() throws SocketException {

        monitorService.evalHealth();
    }




}
