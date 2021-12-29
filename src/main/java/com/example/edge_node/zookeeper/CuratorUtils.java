package com.example.edge_node.zookeeper;

import com.example.edge_node.pojo.Image;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Create by zhangran
 */
@Component
@Slf4j
public class CuratorUtils {
    private int sleepTime ;
    private int MaxRetries;
    public String rootPath;
    private String zkHost ;
    private String zkPort;
    private static CuratorFramework zkClient;

    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();



    public CuratorUtils(@Value("${zookeeper.sleepTime}") int sleepTime,
                        @Value("${zookeeper.maxRetries}") int maxRetries,
                        @Value("${server.nodeName}") String rootPath,
                        @Value("${zookeeper.host}") String zkHost,
                        @Value("${zookeeper.port}") String zkPort) {
        this.sleepTime = sleepTime;
        MaxRetries = maxRetries;
        this.rootPath = rootPath;
        this.zkHost = zkHost;
        this.zkPort = zkPort;

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(sleepTime,maxRetries);
        zkClient = CuratorFrameworkFactory.builder()
                // the server to connect to (can be a server list)
                .connectString(zkHost + ":" + zkPort)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        try {
            // wait 30s until connect to the zookeeper
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("Time out waiting to connect to ZK!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public CuratorFramework getZkClient(){
        return zkClient;
    }

    public void createPersistentNode(String path,String data) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("The node already exists. The node is:[{}]", path);
            } else {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path,data.getBytes());
                log.info("The node was created successfully. The node is:[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("create persistent node for path [{}] fail", path);
        }
    }

    public void createTemporaryNodeWithParent(String path,String data) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("The node already exists. The node is:[{}]", path);
            } else {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path,data.getBytes());
                log.info("The node was created successfully. The node is:[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("create temporary node for path [{}] fail", path);
        }
    }

    public void createTemporaryNode(String path,String data) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("The node already exists. The node is:[{}]", path);
            } else {
                zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(path,data.getBytes());
                log.info("The node was created successfully. The node is:[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("create temporary node for path [{}] fail", path);
        }
    }

    public void createContainerNode(String path,String data) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("The node already exists. The node is:[{}]", path);
            } else {
                zkClient.create().withMode(CreateMode.CONTAINER).forPath(path,data.getBytes());
                log.info("The node was created successfully. The node is:[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("create temporary node for path [{}] fail", path);
        }
    }

    public List<String> getChildrenNodes(String path) {

        List<String> result = null;
        try {
            result = zkClient.getChildren().forPath(path);

        } catch (Exception e) {
            log.error("get children nodes for path [{}] fail", path);
        }
        return result;
    }

    public void clearRegistry() {
        REGISTERED_PATH_SET.stream().forEach(p -> {
            try {
                zkClient.delete().deletingChildrenIfNeeded().forPath(p);
            } catch (Exception e) {
                log.error("clear registry for path [{}] fail", p);
            }
        });
        log.info("All registered on the server are cleared:[{}]", REGISTERED_PATH_SET.toString());
        REGISTERED_PATH_SET.clear();

    }

    public void clearAll(){
        try {
            zkClient.delete().deletingChildrenIfNeeded().forPath("/EdgeCloud/" + rootPath);
        } catch (Exception e) {
            log.error("clear all fail");
        }
        log.info("All nodes on the server are cleared");
        REGISTERED_PATH_SET.clear();
    }
    public void clear(String path){
        try {
            zkClient.delete().deletingChildrenIfNeeded().forPath(path);
            log.info("All nodes on the server are cleared");
        } catch (Exception e) {
            log.error("clear all fail");
        }

    }

    public String getNodeData(String path){
        try {
            return new String(zkClient.getData().forPath(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateNodeData(String path,String data){
        try {
            zkClient.setData().forPath(path,data.getBytes());
        } catch (Exception e) {
            log.error("update fail");
        }
    }
    public void init(){
        String initPath = "/EdgeCloud/" + rootPath;

        createContainerNode(initPath,"");
        createContainerNode(initPath + "/images","");
        createContainerNode(initPath + "/nodeinfo","");
    }

    public void registeImage(Image image){
        String imagePath = "/EdgeCloud/" + rootPath + "/" + "images/" + image.getName();
        String imageId = image.getId();
        createTemporaryNode(imagePath,imageId);
    }

    public void registeInfo(String path,String text){
        if(text == null) return;
        String infoPath = "/EdgeCloud/" + rootPath + "/" + "nodeinfo" + path;
        createTemporaryNodeWithParent(infoPath,text);
    }

    public void registeChildren(String path){
        String infoPath = "/EdgeCloud/" + rootPath + "/" + "nodeinfo" + path;
        createContainerNode(infoPath,"");
    }
}
