package com.example.edge_node.service;

import com.example.edge_node.mapper.ImageMapper;
import com.example.edge_node.pojo.Image;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Create by zhangran
 */
@Service
@Slf4j
public class TaskService {
    @Autowired
    ContainerService containerService;
    @Autowired
    ImageMapper imageMapper;
    @Autowired
    FileService fileService;
    @Autowired
    ImageService imageService;
    @Autowired
    MonitorService monitorService;

    public String startTask(String taskName,String input,String res){
        if("".equals(taskName)){
            log.info("任务名不能为空");
            return "-1";
        }
        List<Image> imgs = imageMapper.getImageByName(taskName);
        List<String> ctns = new ArrayList<String>();
        for (Image img : imgs) {
            String ctnId = UUID.randomUUID().toString();
            containerService.create(img.getId(),ctnId,null);
            ctns.add(ctnId);
            log.info("任务容器创建成功");
        }
        if(!"".equals(input)){
            log.info("存储输入文件");
            fileService.saveInput(input);
            log.info("添加输入文件到容器");
            for (String ctn : ctns) {
                String workDir = Objects.requireNonNull(imageService.info(monitorService.inspectContainer(ctn).getImageId()).getContainerConfig()).getWorkingDir();
                fileService.copyFileToContainer(ctn,fileService.getFilepath() + "input.txt",  workDir + "/");

            }
        }
        log.info("容器启动成功");
        String ans = "";
        while(!ctns.isEmpty()){
            String ctnId = ctns.get(0);

            String status = monitorService.inspectContainer(ctnId).getState().getStatus();
            if("exited".equals(status)){
                if("yes".equals(res)){
                    try {
                        String workDir = Objects.requireNonNull(imageService.info(monitorService.inspectContainer(ctnId).getImageId()).getContainerConfig()).getWorkingDir();
                        ans = fileService.copyFromContainer(ctnId,workDir + "/res.txt");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                containerService.delete(ctnId);
                ctns.remove(0);
                log.info("删除容器：" + ctnId);
            }
        }
        return ans;
    }
}
