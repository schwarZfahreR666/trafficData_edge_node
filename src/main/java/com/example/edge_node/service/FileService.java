package com.example.edge_node.service;

import com.example.edge_node.utils.ZipUtils;
import com.github.dockerjava.api.DockerClient;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
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

    public String getFilepath(){
        return filepath;
    }

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
            log.error("dockerfile存储失败！");
            return -1;
        }
        log.info("dockerfile存储成功！");
        return 0;
    }

    public int saveInput(String content){

        File targetFile = new File(filepath);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        try (PrintStream out = new PrintStream(new FileOutputStream(filepath + "input.txt"))) {
            out.print(content);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.error("input存储失败！");
            return -1;
        }
        log.info("input存储成功！");
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

    public String copyFromContainer(String id,String remotePath) throws IOException {
        DockerClient cli = dockerService.getDockerClient();
        if (cli == null)
            return null;
//        InputStream response = cli.copyArchiveFromContainerCmd(id, remotePath).exec();
        TarArchiveInputStream tarStream = new TarArchiveInputStream(cli.copyArchiveFromContainerCmd(id, remotePath).exec());

        // read the stream fully. Otherwise, the underlying stream will not be closed.
        return unTar(tarStream);
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

    public String inputStreamToString(InputStream is) {

        try {
            ByteArrayOutputStream boa = new ByteArrayOutputStream();
            int len = 0;
            byte[] buffer = new byte[1024];

            while((len = is.read(buffer))!=-1){
                boa.write(buffer,0,len);
            }
            is.close();
            boa.close();
            byte[] result = boa.toByteArray();

            String temp = new String(result);
            //识别编码
            if(temp.contains("utf-8")){
                return new String(result,"utf-8");
            }else if(temp.contains("gb2312")){
                return new String(result,"gb2312");
            }else{
                return new String(result,"utf-8");
            }


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

    }

    public String unTar(TarArchiveInputStream tis)
            throws IOException {
        TarArchiveEntry tarEntry = null;
        String res = "";
        while ((tarEntry = tis.getNextTarEntry()) != null) {
            if (!tarEntry.isDirectory()){

                ByteArrayOutputStream boa = new ByteArrayOutputStream();
                int len = 0;
                byte[] buffer = new byte[1024];
                while((len = tis.read(buffer))!=-1){
                    boa.write(buffer,0,len);
                }
                boa.close();
                byte[] result = boa.toByteArray();
                String temp = new String(result);
                //识别编码
                if(temp.contains("utf-8")){
                    res += new String(result,"utf-8");
                }else if(temp.contains("gb2312")){
                    res += new String(result,"gb2312");
                }else{
                    res += new String(result,"utf-8");
                }

            }
        }
        tis.close();
        return res;
    }
}
