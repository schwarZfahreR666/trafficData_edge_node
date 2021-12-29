package com.example.edge_node.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.edge_node.pojo.NodeInfo;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Create by zhangran
 */
public class JSONReader {
    public static NodeInfo readJSON(String path){

        String sets = ReadFile(path);
        JSONObject jo = JSONObject.parseObject(sets);// 格式化成json对象
        return traverseBuild(jo);
    }
    private static NodeInfo traverseBuild(JSONObject json){
        NodeInfo info = new NodeInfo();
        info.setId(json.getString("id"));
        info.setInfo(json.getString("info"));
        info.setName(json.getString("name"));
        info.setType(json.getString("type"));
        info.setUrl(json.getString("url"));
        info.setImg(json.getString("img"));
        JSONArray array = json.getJSONArray("children");
        if(array != null && !array.isEmpty()){
            List<NodeInfo> children = new ArrayList<>();
            for(int i = 0;i < array.size();i++){
                children.add(traverseBuild(array.getJSONObject(i)));
            }
            info.setChildren(children);
        }
        return info;
    }
    // 读文件，返回字符串
    private static String ReadFile(String path) {
//        File file = new File(path);
//        File file = new File(JSONReader.class.getResource(path).getFile());
        InputStream inputStream = JSONReader.class.getResourceAsStream(path);
        File file = new File("resource-topo.json");
        try {
            FileUtils.copyInputStreamToFile(inputStream,file);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        BufferedReader reader = null;
        String laststr = "";
        try {
            // System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                laststr = laststr + tempString;
                line++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return laststr;
    }
}
