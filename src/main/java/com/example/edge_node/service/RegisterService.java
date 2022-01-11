package com.example.edge_node.service;

import com.example.edge_node.config.MasterCondition;
import com.example.edge_node.mapper.ImageMapper;
import com.example.edge_node.pojo.Image;
import com.example.edge_node.pojo.NodeInfo;
import com.example.edge_node.utils.Base64ImageUtils;
import com.example.edge_node.utils.JSONReader;
import com.example.edge_node.zookeeper.CuratorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Create by zhangran
 */
@Conditional(MasterCondition.class)
@Service
@Slf4j
public class RegisterService {
    @Autowired
    CuratorUtils curatorUtils;
    @Autowired
    ImageMapper imageMapper;
    @Autowired
    RedisTemplate defaultRedisTemplate;
    @Value("${server.NodeInfoPath}")
    String path;

    public void registeAllTask(){
        curatorUtils.clearAll();
        curatorUtils.init();
        List<Image> images = imageMapper.getImages();
        for (com.example.edge_node.pojo.Image image : images) {
            curatorUtils.registeImage(image);
        }

        registeNodeInfo();
    }

    public void imgRegiste(String imgName){
        String encoderStr = Base64ImageUtils.encodeImgageToBase64("/static/data/" + imgName);

        defaultRedisTemplate.opsForValue().set(imgName,encoderStr);
    }

    public void registeNodeInfo(){
        NodeInfo nodeinfo = JSONReader.readJSON(path);
        traverseRegiste(nodeinfo,"/");

    }

    private void traverseRegiste(NodeInfo nodeinfo,String parentPath){

        curatorUtils.registeInfo(parentPath + "id",nodeinfo.getId());
        curatorUtils.registeInfo(parentPath +  "name",nodeinfo.getName());
        curatorUtils.registeInfo(parentPath  + "type",nodeinfo.getType());
        curatorUtils.registeInfo(parentPath  + "url",nodeinfo.getUrl());
        curatorUtils.registeInfo(parentPath  + "img",nodeinfo.getImg());
        if(nodeinfo.getImg() != null) imgRegiste(nodeinfo.getImg());
        curatorUtils.registeInfo(parentPath  + "info",nodeinfo.getInfo());
        List<NodeInfo> children = nodeinfo.getChildren();
        if(children != null ){
            for(int i = 0;i < children.size(); i++ ){
                curatorUtils.registeChildren(parentPath + "children");
                curatorUtils.registeChildren(parentPath + "children/child" + i);
                traverseRegiste(children.get(i),parentPath + "children/child" + i + "/");
            }
        }
    }


}
