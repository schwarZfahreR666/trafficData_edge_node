package com.example.edge_node;

import com.example.edge_node.cluster.dto.Message;
import com.example.edge_node.mapper.ImageMapper;
import com.example.edge_node.neo4j.BaseService;
import com.example.edge_node.service.*;
import com.example.edge_node.utils.Base64ImageUtils;
import com.example.edge_node.utils.NetUtils;
import com.example.edge_node.utils.ZipUtils;
import com.example.edge_node.zookeeper.CuratorUtils;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.neo4j.driver.Values.parameters;

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
    @Autowired
    CuratorUtils curatorUtils;
    @Autowired
    RegisterService registerService;
    @Autowired
    RedisTemplate defaultRedisTemplate;
    @Autowired
    OfflineService offlineService;
    @Autowired
    SlaveService slaveService;
    @Autowired
    MasterService masterService;


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

        System.out.println(monitorService.evalHealth());
    }


    @Test
    void testNeo4j(){

        Session session = BaseService.DATASOURCE.getSession();
        Result result = session.run( "MATCH (c:node)-[:to]->(s:node) where c.name=$stop_name and c.lower_bound<=$stop_var and c.upper_bound>$stop_var return s.var as score" +
                        " UNION ALL " +
                        "MATCH (c:node)-[:to]->(s:node) where c.name=$sys_name and c.lower_bound<=$sys_var and c.upper_bound>$sys_var return s.var as score" +
                        " UNION ALL " +
                        "MATCH (c:node)-[:to]->(s:node) where c.name=$mem_name and c.lower_bound<=$mem_var and c.upper_bound>$mem_var return s.var as score" +
                        " UNION ALL " +
                        "MATCH (c:node)-[:to]->(s:node) where c.name=$use_name and c.lower_bound<=$use_var and c.upper_bound>$use_var return s.var as score" +
                        " UNION ALL " +
                        "MATCH (c:node)-[:to]->(s:node) where c.name=$wait_name and c.lower_bound<=$wait_var and c.upper_bound>$wait_var return s.var as score",
                parameters(
                        "stop_name", "containersStopped" ,"stop_var",1,
                        "sys_name","cpuSys","sys_var",19.5,
                        "mem_name","MemUsage","mem_var",20.1,
                        "use_name","cpuUsed","use_var",22.3,
                        "wait_name","cpuWait","wait_var",26.5
                ) );





        System.out.println(result.list());

        BaseService.DATASOURCE.close();

    }


    @Test
    public void testZk() throws Exception {
        curatorUtils.clear("/EdgeCloud/YANQING");
//        List<com.example.edge_node.pojo.Image> images = imageMapper.getImages();
//        for (com.example.edge_node.pojo.Image image : images) {
//            curatorUtils.registeImage(image);
//        }
//
//        List<String> res = curatorUtils.getChildrenNodes("/EdgeCloud/BUAA/images");
//        for (String name : res) {
//            System.out.println(curatorUtils.getNodeData("/EdgeCloud/BUAA/images/" + name));
//        }
//
//        curatorUtils.clearRegistry();
//        res = curatorUtils.getChildrenNodes("/EdgeCloud/BUAA/images");
//        for (String name : res) {
//            System.out.println(name);
//        }

    }
    @Test
    public void registeInfo(){
        registerService.registeAllTask();
    }

    @Test
    public void testRedis(){
        String encoderStr = Base64ImageUtils.encodeImgageToBase64("/static/data/beihang.jpg");

        defaultRedisTemplate.opsForValue().set("beihang.jpg",encoderStr);

        String ans = (String) defaultRedisTemplate.opsForValue().get("beihang.jpg");

        Base64ImageUtils.decodeBase64ToImage(ans,"./","cache.png");

        System.out.println(encoderStr.equals(ans));


    }
    @Test
    public void testOffline() throws InterruptedException {
        System.out.println(NetUtils.isConnect("47.95.159.86"));
        offlineService.putTask("1111",5);
        offlineService.putTask("222",5);
        offlineService.putTask("333",5);
        offlineService.scheduleTask();
    }

    @Test
    public void testCluster(){
        slaveService.sendHealth();
        Message message = Message.builder().taskName("road_info").build();
        masterService.sendMessage2slave("123",message);
    }




}
