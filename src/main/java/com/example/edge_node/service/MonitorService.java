package com.example.edge_node.service;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.edge_node.neo4j.BaseService;
import com.example.edge_node.neo4j.BaseSession;
import com.example.edge_node.pojo.*;
import com.example.edge_node.utils.IpUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.HealthState;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.Statistics;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.neo4j.driver.Values.parameters;

/**
 * Create by zhangran
 */
@Service
@Slf4j
public class MonitorService {
    @Autowired
    DockerService dockerService;
    @Autowired
    ContainerService containerService;
    @Value("${docker.restUrl}")
    private String url;
    /*获取单个container信息*/
    public Status getStats(String id){
        String statistics = null;

        String containerID = id;
        Status status = new Status();

        RestTemplate restTemplate =  new RestTemplate();
        statistics = restTemplate.getForObject(url + "/containers/"+containerID+"/stats?stream=0", String.class);
        if(statistics == null) return status;
        System.out.println(statistics);
        JSONObject jsonObject =  JSONObject.parseObject(statistics);

        JSONObject memory_stats = jsonObject.getJSONObject("memory_stats");
        if(memory_stats != null){
            Float usage = memory_stats.getFloat("usage");
            Float cache = null;
            JSONObject stats = memory_stats.getJSONObject("stats");
            if(stats != null) {
                cache = stats.getFloat("cache");
            }
            float used_memory = 0;
            if(usage != null && cache != null){
                used_memory = (usage - cache) / 1024;
            }
            float available_memory = 0.01f;
            if(memory_stats.getFloat("limit") != null){
                available_memory = memory_stats.getFloat("limit") / 1024;
            }
            float memory_usage = (used_memory / available_memory) * 100;
            status.setAvailable_memory(available_memory);
            status.setMemory_usage(memory_usage);
            status.setUsed_memory(used_memory);
        }
        JSONObject cpu_stats = jsonObject.getJSONObject("cpu_stats");
        if(cpu_stats != null){
            JSONObject _cpu_usage = cpu_stats.getJSONObject("cpu_usage");
            Float system_cpu_usage = cpu_stats.getFloat("system_cpu_usage");
            JSONObject precpu_stats = jsonObject.getJSONObject("precpu_stats");
            float cpu_delta = 0;
            if(_cpu_usage != null && _cpu_usage.getFloat("total_usage") != null && precpu_stats != null && precpu_stats.getJSONObject("cpu_usage") != null && precpu_stats.getJSONObject("cpu_usage").getFloat("total_usage") != null){
                cpu_delta = (cpu_stats.getJSONObject("cpu_usage").getFloat("total_usage") - jsonObject.getJSONObject("precpu_stats").getJSONObject("cpu_usage").getFloat("total_usage")) / 1024;
            }
            float system_cpu_delta = 0.01f;
            if(cpu_stats.getFloat("system_cpu_usage") != null && precpu_stats.getFloat("system_cpu_usage") != null){
                system_cpu_delta = (cpu_stats.getFloat("system_cpu_usage") - jsonObject.getJSONObject("precpu_stats").getFloat("system_cpu_usage")) / 1024;
            }
            int number_cpus = 0;
            if(_cpu_usage.getJSONArray("percpu_usage") != null){
                number_cpus = _cpu_usage.getJSONArray("percpu_usage").size();
            }
            float cpu_usage = (cpu_delta / system_cpu_delta) * number_cpus * 100;

            status.setCpu_delta(cpu_delta);
            status.setCpu_usage(cpu_usage);

            status.setNumber_cpus(number_cpus);

            status.setSystem_cpu_delta(system_cpu_delta);
        }







        return status;

    }

    /*获取单个container的inspect*/
    public String getInspect(String id){
        String statistics = null;

        String containerID = id;

        RestTemplate restTemplate =  new RestTemplate();
        statistics = restTemplate.getForObject(url + "/containers/"+containerID+"/json", String.class);

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
    public double getHealthScore() throws SocketException {

        SysMonitor sysMonitor = SystemMonitor();

        HostInfo hostInfo = getInfo();

        List<Container> ctns = containerService.list();
        List<String> ctnHealths = new ArrayList<>();
        for (Container ctn : ctns) {
            String id = ctn.getId();

            InspectContainerResponse inspectContainerResponse = inspectContainer(id);
            if(inspectContainerResponse == null) continue;
            String status = inspectContainerResponse.getState().getStatus();
            if("running".equals(status)){
                HealthState healthState = inspectContainerResponse.getState().getHealth();
                String health = "unhealthy";
                if(healthState != null){
                    health = healthState.getStatus();
                }
                float cpu_usage = getStats(id).getCpu_usage();


                Boolean dead = inspectContainerResponse.getState().getDead();
                Boolean oomKilled = inspectContainerResponse.getState().getOOMKilled();
                int exitCode = inspectContainerResponse.getState().getExitCode();


//                Session session = BaseService.DATASOURCE.getSession();
                BaseSession baseSession = new BaseSession();
                Session session = baseSession.getSession();
                Result result = session.run( "MATCH (c:container)-[:to]->(s:container) where c.name=$cpu_usage_name and c.lower_bound<=$cpu_usage_var and c.upper_bound>$cpu_usage_var return s.status as status" +
                                " UNION ALL " +
                                "MATCH (c:container)-[:to]->(s:container) where c.name=$health_name and c.status=$health_var return s.status as status" +
                                " UNION ALL " +
                                "MATCH (c:container)-[:to]->(s:container) where c.name=$oom_name and c.status=$oom_var return s.status as status" +
                                " UNION ALL " +
                                "MATCH (c:container)-[:to]->(s:container) where c.name=$dead_name and c.status=$dead_var return s.status as status" +
                                " UNION ALL " +
                                "MATCH (c:container)-[:to]->(s:container) where c.name=$exit_name and c.lower_bound<=$exit_var and c.upper_bound>$exit_var return s.status as status",
                        parameters(
                                "cpu_usage_name", "cpu_usage" ,"cpu_usage_var",cpu_usage,
                                "oom_name","OOMkilled","oom_var",oomKilled.toString(),
                                "health_name","health","health_var",health,
                                "dead_name","dead","dead_var",dead.toString(),
                                "exit_name","ExitCode","exit_var",exitCode
                        ) );



                Long state = result.list().stream().map(r->r.get("status").toString().replace("\"","")).filter(r->"unhealthy".equals(r)).count();
                baseSession.close();
                if(state >= 1){
                    ctnHealths.add("unhealthy");
                }else{
                    ctnHealths.add("healthy");
                }

            }

        }


        int containersStopped = hostInfo.getContainersStopped();

        int containersRunning = hostInfo.getContainersRunning();

        int healthyCount = (int)ctnHealths.stream().filter("healthy"::equals).count();
        float cpuSys = (float)sysMonitor.getCpuSys();
        float memUsage = (float)sysMonitor.getMemUsage();
        float cpuUsed = (float)sysMonitor.getCpuUsed();
        float cpuWait = (float)sysMonitor.getCpuWait();


//        Session session = BaseService.DATASOURCE.getSession();
        BaseSession baseSession = new BaseSession();
        Session session = baseSession.getSession();
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
                        "stop_name", "containersStopped" ,"stop_var",containersStopped,
                        "sys_name","cpuSys","sys_var",cpuSys,
                        "mem_name","MemUsage","mem_var",memUsage,
                        "use_name","cpuUsed","use_var",cpuUsed,
                        "wait_name","cpuWait","wait_var",cpuWait
                ) );



        List<Integer> scores = result.stream().map(r -> r.get("score").asInt()).collect(Collectors.toList());
        baseSession.close();
        double totalScore = 0;
        String eval = "节点";
        for(int i=0;i<scores.size();i++){
            int score = scores.get(i);
            totalScore += score;

        }

        totalScore = ((float)healthyCount+1.0)/((float)containersRunning+1.0) * (float)totalScore;




//        BaseService.DATASOURCE.close();

        return totalScore;
    }


    public String evalHealth() throws SocketException {
        SysMonitor sysMonitor = SystemMonitor();

        HostInfo hostInfo = getInfo();

        List<Container> ctns = containerService.list();
        List<String> ctnHealths = new ArrayList<>();
        for (Container ctn : ctns) {
            String id = ctn.getId();

            InspectContainerResponse inspectContainerResponse = inspectContainer(id);
            String status = inspectContainerResponse.getState().getStatus();
            if("running".equals(status)){
                HealthState healthState = inspectContainerResponse.getState().getHealth();
                String health = "unhealthy";
                if(healthState != null){
                    health = healthState.getStatus();
                }
                float cpu_usage = getStats(id).getCpu_usage();


                Boolean dead = inspectContainerResponse.getState().getDead();
                Boolean oomKilled = inspectContainerResponse.getState().getOOMKilled();
                int exitCode = inspectContainerResponse.getState().getExitCode();


//                Session session = BaseService.DATASOURCE.getSession();
                BaseSession baseSession = new BaseSession();
                Session session = baseSession.getSession();
                Result result = session.run( "MATCH (c:container)-[:to]->(s:container) where c.name=$cpu_usage_name and c.lower_bound<=$cpu_usage_var and c.upper_bound>$cpu_usage_var return s.status as status" +
                                " UNION ALL " +
                                "MATCH (c:container)-[:to]->(s:container) where c.name=$health_name and c.status=$health_var return s.status as status" +
                                " UNION ALL " +
                                "MATCH (c:container)-[:to]->(s:container) where c.name=$oom_name and c.status=$oom_var return s.status as status" +
                                " UNION ALL " +
                                "MATCH (c:container)-[:to]->(s:container) where c.name=$dead_name and c.status=$dead_var return s.status as status" +
                                " UNION ALL " +
                                "MATCH (c:container)-[:to]->(s:container) where c.name=$exit_name and c.lower_bound<=$exit_var and c.upper_bound>$exit_var return s.status as status",
                        parameters(
                                "cpu_usage_name", "cpu_usage" ,"cpu_usage_var",cpu_usage,
                                "oom_name","OOMkilled","oom_var",oomKilled.toString(),
                                "health_name","health","health_var",health,
                                "dead_name","dead","dead_var",dead.toString(),
                                "exit_name","ExitCode","exit_var",exitCode
                        ) );




                Long state = result.list().stream().map(r->r.get("status").toString().replace("\"","")).filter(r->"unhealthy".equals(r)).count();
                baseSession.close();
                if(state >= 1){
                    ctnHealths.add("unhealthy");
                }else{
                    ctnHealths.add("healthy");
                }

            }

        }




        int containersStopped = hostInfo.getContainersStopped();

        int containersRunning = hostInfo.getContainersRunning();

        int healthyCount = (int)ctnHealths.stream().filter(a -> "healthy".equals(a)).count();
        float cpuSys = (float)sysMonitor.getCpuSys();
        float memUsage = (float)sysMonitor.getMemUsage();
        float cpuUsed = (float)sysMonitor.getCpuUsed();
        float cpuWait = (float)sysMonitor.getCpuWait();


//        Session session = BaseService.DATASOURCE.getSession();
        BaseSession baseSession = new BaseSession();
        Session session = baseSession.getSession();
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
                        "stop_name", "containersStopped" ,"stop_var",containersStopped,
                        "sys_name","cpuSys","sys_var",cpuSys,
                        "mem_name","MemUsage","mem_var",memUsage,
                        "use_name","cpuUsed","use_var",cpuUsed,
                        "wait_name","cpuWait","wait_var",cpuWait
                ) );



        String[] ITEM = {"任务运行情况","系统资源占用情况","内存使用情况","CPU使用情况","任务阻塞情况"};
        List<Integer> scores = null;
        if(result.hasNext()){
            scores = result.stream().map(r -> r.get("score").asInt()).collect(Collectors.toList());
        }
        else{
            scores = new ArrayList(Collections.singleton(new int[]{15, 15, 15, 15, 15}));
        }
        baseSession.close();
        double totalScore = 0;
        String eval = "节点";
        for(int i=0;i<scores.size();i++){
            int score = scores.get(i);
            if(5<score && score<=15){
                eval += ITEM[i] + "不太好，";
            }else if(score<=5){
                eval += ITEM[i] + "比较差，";
            }
            totalScore += score;

        }

        totalScore = ((float)healthyCount+1.0)/((float)containersRunning+1.0) * (float)totalScore;


        eval += "健康评分为：" + totalScore;



//        BaseService.DATASOURCE.close();

        return eval;


    }

}
