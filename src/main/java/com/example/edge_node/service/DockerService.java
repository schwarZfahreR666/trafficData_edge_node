package com.example.edge_node.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Create by zhangran
 */
@Service
public class DockerService {

    private DockerClient dockerClient;
    @Value("${docker.url}")
    private String url;

    public DockerService(@Value("${docker.url}") String url) {
        this.dockerClient = DockerClientBuilder.getInstance(url).build();
    }
    public DockerClient getDockerClient(){
        return this.dockerClient;

    }

    public void closeDockerClient() throws IOException {
        this.dockerClient.close();
    }

}
