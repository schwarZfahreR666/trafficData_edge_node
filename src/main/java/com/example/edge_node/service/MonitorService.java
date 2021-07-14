package com.example.edge_node.service;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.edge_node.pojo.*;
import com.example.edge_node.utils.IpUtil;
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
import oshi.SystemInfo;


import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.util.Util;
import oshi.hardware.CentralProcessor.TickType;

import java.net.SocketException;
import java.util.Properties;

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


    public SysMonitor SystemMonitor() throws SocketException {
        final long serialVersionUID = 1L;

        final int OSHI_WAIT_SECOND = 1000;


        SysMonitor sys = new SysMonitor();

        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();

        CentralProcessor processor = hal.getProcessor();

        /**
         * 设置CPU信息
         */

        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(OSHI_WAIT_SECOND);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[TickType.NICE.getIndex()] - prevTicks[TickType.NICE.getIndex()];
        long irq = ticks[TickType.IRQ.getIndex()] - prevTicks[TickType.IRQ.getIndex()];
        long softirq = ticks[TickType.SOFTIRQ.getIndex()] - prevTicks[TickType.SOFTIRQ.getIndex()];
        long steal = ticks[TickType.STEAL.getIndex()] - prevTicks[TickType.STEAL.getIndex()];
        long cSys = ticks[TickType.SYSTEM.getIndex()] - prevTicks[TickType.SYSTEM.getIndex()];
        long user = ticks[TickType.USER.getIndex()] - prevTicks[TickType.USER.getIndex()];
        long iowait = ticks[TickType.IOWAIT.getIndex()] - prevTicks[TickType.IOWAIT.getIndex()];
        long idle = ticks[TickType.IDLE.getIndex()] - prevTicks[TickType.IDLE.getIndex()];
        long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
        sys.setCpuNum(processor.getLogicalProcessorCount());
        sys.setCpuTotal(totalCpu);
        sys.setCpuSys(cSys);
        sys.setCpuUsed(user);
        sys.setCpuWait(iowait);
        sys.setCpuFree(idle);

        /**
         * 设置内存信息
         */
        GlobalMemory memory = hal.getMemory();

        sys.setMemTotal(NumberUtil.div(memory.getTotal(), (1024 * 1024 * 1024), 2));
        sys.setMemUsed(NumberUtil.div(memory.getTotal() - memory.getAvailable(), (1024 * 1024 * 1024), 2));
        sys.setMemFree(NumberUtil.div(memory.getAvailable(), (1024 * 1024 * 1024), 2));
        sys.setMemUsage(NumberUtil.mul(NumberUtil.div(memory.getTotal() - memory.getAvailable(), memory.getTotal(), 4), 100));

        /**
         * 设置服务器信息
         */

        Properties props = System.getProperties();
        sys.setComputerName(IpUtil.getHostIp());
        sys.setComputerIp(NetUtil.getLocalhostStr());
        sys.setOsName(props.getProperty("os.name"));
        sys.setOsArch(props.getProperty("os.arch"));
        sys.setUserDir(props.getProperty("user.dir"));

        return sys;

    }

}
