package com.example.edge_node.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.edge_node.pojo.HostInfo;
import com.example.edge_node.pojo.Status;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Create by zhangran
 */
@Service
public class MonitorService {
    @Autowired
    DockerService dockerService;
    @Value("${docker.restUrl}")
    private String url;
    /*获取单个container信息*/
    public Status getStats(String id){
        String statistics = null;

        String containerID = id;

        RestTemplate restTemplate =  new RestTemplate();
        statistics = restTemplate.getForObject(url.replace("tcp","http") + "/containers/"+containerID+"/stats?stream=0", String.class);
        JSONObject jsonObject =  JSONObject.parseObject(statistics);
        float used_memory = (jsonObject.getJSONObject("memory_stats").getFloat("usage") - jsonObject.getJSONObject("memory_stats").getJSONObject("stats").getFloat("cache")) / 1024;
        float available_memory = jsonObject.getJSONObject("memory_stats").getFloat("limit") / 1024;
        float memory_usage = (used_memory / available_memory) * 100;
        float cpu_delta = (jsonObject.getJSONObject("cpu_stats").getJSONObject("cpu_usage").getFloat("total_usage") - jsonObject.getJSONObject("precpu_stats").getJSONObject("cpu_usage").getFloat("total_usage")) / 1024;
        float system_cpu_delta = (jsonObject.getJSONObject("cpu_stats").getFloat("system_cpu_usage") - jsonObject.getJSONObject("precpu_stats").getFloat("system_cpu_usage")) / 1024;
        int number_cpus = jsonObject.getJSONObject("cpu_stats").getJSONObject("cpu_usage").getJSONArray("percpu_usage").size();
        float cpu_usage = (cpu_delta / system_cpu_delta) * number_cpus * 100;
        Status status = new Status();
        status.setAvailable_memory(available_memory);
        status.setCpu_delta(cpu_delta);
        status.setCpu_usage(cpu_usage);
        status.setMemory_usage(memory_usage);
        status.setNumber_cpus(number_cpus);
        status.setUsed_memory(used_memory);
        status.setSystem_cpu_delta(system_cpu_delta);

        return status;

    }

    /*获取单个container的inspect*/
    public String getInspect(String id){
        String statistics = null;

        String containerID = id;

        RestTemplate restTemplate =  new RestTemplate();
        statistics = restTemplate.getForObject(url.replace("tcp","http") + "/containers/"+containerID+"/json", String.class);

        JSONObject jsonObject =  JSONObject.parseObject(statistics);
        String running = jsonObject.getJSONObject("State").getString("Running");


        return statistics;

    }

    public InspectContainerResponse inspectContainer(String id){
        DockerClient cli = dockerService.getDockerClient();
        if (cli == null)
            return null;
        InspectContainerResponse containerInfo = cli.inspectContainerCmd(id)
//                .withContainerId(id)
                .exec();

        return containerInfo;
    }

    /*获取整体docker信息*/
    public HostInfo getInfo(){
        DockerClient cli = dockerService.getDockerClient();
        if (cli == null)
            return null;
        Info dockerInfo = cli.infoCmd().exec();
        HostInfo hostInfo = new HostInfo();
        hostInfo.setContainers(dockerInfo.getContainers());
        hostInfo.setContainersRunning(dockerInfo.getContainersRunning());
        hostInfo.setContainersStopped(dockerInfo.getContainersStopped());
        hostInfo.setImages(dockerInfo.getImages());
        hostInfo.setMemTotal(dockerInfo.getMemTotal() / 1024 );
        hostInfo.setContainersPaused(dockerInfo.getContainersPaused());

        return hostInfo;
    }
}
