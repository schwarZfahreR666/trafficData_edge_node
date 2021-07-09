package com.example.edge_node.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.model.Image;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Create by zhangran
 */
@Service
public class ImageService {

    @Value("${docker.filepath}")
    private String files;

    @Autowired
    private DockerService dockerService;

    /*获取镜像列表*/
    public List<Image> list() {
        DockerClient cli = dockerService.getDockerClient();
        if (cli == null)
            return new ArrayList<Image>();
        return cli.listImagesCmd().exec();
    }

    /*获取指定镜像信息*/
    public InspectImageResponse info(String name) {
        DockerClient cli = dockerService.getDockerClient();
        if (cli == null)
            return null;
        return cli.inspectImageCmd(name).exec();
    }
    /*删除指定镜像*/
    public void delete(String name) {
        DockerClient cli = dockerService.getDockerClient();
        if (cli == null)
            return;
        cli.removeImageCmd(name).exec();
    }


    /*根据Dockerfile创建容器*/
    public void buildImage(String id) throws Exception {
        DockerClient cli = dockerService.getDockerClient();
        if (cli == null)
            return;
        System.out.println(files);
        File baseDir = new File(files);

        Collection<File> files = FileUtils.listFiles(baseDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        File dockerfile = new File(baseDir + "/dockerfile");
        //可以把dockerfile转存再编译,要add的文件放到basedir下即可
        cli.buildImageCmd()
                .withNoCache(true)
                .withTag(id)
                .withBaseDirectory(baseDir)
                .withDockerfile(dockerfile)
                .start()
                .awaitImageId();


    }

    private File fileFromBuildTestResource(String resource) {

        return new File(Thread.currentThread().getContextClassLoader()
                .getResource(resource).getFile());

    }


}
