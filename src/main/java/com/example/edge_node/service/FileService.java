package com.example.edge_node.service;

import com.example.edge_node.utils.ZipUtils;
import com.github.dockerjava.api.DockerClient;

import com.mysql.cj.util.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Create by zhangran
 */
@Service
@Slf4j
public class FileService {

    @Autowired
    DockerService dockerService;

    @Value("${filepath}")
    private String filepath;

    public int saveFiles(MultipartFile[] files) {

        File targetFile = new File(filepath);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        for (MultipartFile file : files) {
            try (FileOutputStream out = new FileOutputStream(filepath + file.getOriginalFilename());) {
                out.write(file.getBytes());
                if (file.getOriginalFilename().contains(".zip")){
                    Path zipFile = Paths.get(filepath + file.getOriginalFilename());
                    Path targetPath = Paths.get(filepath);
                    ZipUtils.unPacket(zipFile,targetPath);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("文件上传失败!");
                return -1;
            }
        }
        log.info("文件上传成功!");
        return 0;
    }

    public int saveDockerfile(String content){

        File targetFile = new File(filepath);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        try (PrintStream out = new PrintStream(new FileOutputStream(filepath + "dockerfile"))) {
            out.print(content);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public void copyTarToContainer(String id,String filepath,String remotePath){
        DockerClient cli = dockerService.getDockerClient();
        if (cli == null)
            return ;
        Path targetPath = Paths.get(filepath);
        try (InputStream uploadStream = Files.newInputStream(targetPath)) {
            cli.copyArchiveToContainerCmd(id)
                    .withRemotePath(remotePath)
                    .withTarInputStream(uploadStream)
                    .exec();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void copyFileToContainer(String id,String filepath,String remotePath){
        DockerClient cli = dockerService.getDockerClient();
        if (cli == null)
            return ;
        cli.copyArchiveToContainerCmd(id)
                .withRemotePath(remotePath)
                .withHostResource(filepath)
                .exec();
    }

    public String copyFromContainer(String id,String filepath){
        DockerClient cli = dockerService.getDockerClient();
        if (cli == null)
            return null;
        InputStream response = cli.copyArchiveFromContainerCmd(id, filepath).exec();

        // read the stream fully. Otherwise, the underlying stream will not be closed.
        return consumeAsString(response);
    }

    public String consumeAsString(InputStream response) {

        StringWriter logwriter = new StringWriter();

        try {
            LineIterator itr = IOUtils.lineIterator(response, "UTF-8");

            while (itr.hasNext()) {
                String line = itr.next();
                logwriter.write(line + (itr.hasNext() ? "\n" : ""));
            }
            response.close();

            return logwriter.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(response);
        }
    }
}
