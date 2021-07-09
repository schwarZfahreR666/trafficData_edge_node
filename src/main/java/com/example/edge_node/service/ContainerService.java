package com.example.edge_node.service;

import com.example.edge_node.utils.Constants;
import com.example.edge_node.utils.LogContainerTestCallback;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Create by zhangran
 */
@Service
public class ContainerService {
    @Autowired
    DockerService dockerService;

    public List<Container> list() {
        DockerClient cli = dockerService.getDockerClient();
        if (cli == null)
            return new ArrayList<Container>();
        return cli.listContainersCmd().withShowAll(true).exec();
    }

    public InspectContainerResponse info(String id) {
        DockerClient cli = dockerService.getDockerClient();
        if (cli == null)
            return null;
        return cli.inspectContainerCmd(id).exec();
    }

    public void stop(String id) {
        DockerClient cli = dockerService.getDockerClient();
        if (cli == null)
            return;
        cli.stopContainerCmd(id).exec();
    }

    public void delete(String id) {
        DockerClient cli = dockerService.getDockerClient();
        if (cli == null)
            return;
        cli.removeContainerCmd(id).exec();
    }

    public void pause(String id) {
        DockerClient cli = dockerService.getDockerClient();
        if (cli == null)
            return;
        cli.pauseContainerCmd(id).exec();
    }

    public void unPause(String id) {
        DockerClient cli = dockerService.getDockerClient();
        if (cli == null)
            return;
        cli.unpauseContainerCmd(id).exec();
    }

    /**/
    public void commit(String id) {
        DockerClient cli = dockerService.getDockerClient();
        if (cli == null)
            return;
        cli.commitCmd(id).exec();
    }

    /*杀死容器*/
    public void kill(String id) {
        DockerClient cli = dockerService.getDockerClient();
        if (cli == null)
            return;
        cli.killContainerCmd(id).exec();
    }

    /*启动容器*/
    public void containerStart(String id) {
        DockerClient cli = dockerService.getDockerClient();
        if (cli == null)
            return;
        cli.startContainerCmd(id);
    }

    /*重启容器*/
    public void restart(String id) {
        DockerClient cli = dockerService.getDockerClient();
        if (cli == null)
            return;
        cli.restartContainerCmd(id).exec();
    }

    /*获取容器运行日志字符串*/
    public String log(String id) throws IOException, InterruptedException {
        DockerClient cli = dockerService.getDockerClient();
        if (cli == null)
            return null;
        LogContainerTestCallback loggingCallback = new LogContainerTestCallback(true);
        cli.logContainerCmd(id)
                .withStdErr(true)
                .withStdOut(true)
                .withTimestamps(true)
                .exec(loggingCallback);
        loggingCallback.awaitCompletion(3, TimeUnit.SECONDS);
        String res = loggingCallback.toString();
        return res;

    }



    /**
     * create a new container from a image, and run it after build completed
     *
     * @param image
     * @param name
     * @param cmd
     */
    public void create(String image, String name, String cmd) {
        DockerClient cli = dockerService.getDockerClient();
        if (cli == null)
            return;
        CreateContainerCmd ccc = cli.createContainerCmd(image);
        ccc.withName(name);
        if (cmd != null && !"".equals(cmd))
            ccc.withCmd(cmd);
        CreateContainerResponse ccr = ccc.exec();
        String id = ccr.getId();
        StartContainerCmd scc = cli.startContainerCmd(id);
        scc.exec();
    }
}
