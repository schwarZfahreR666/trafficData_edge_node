package com.example.edge_node.mapper;

import com.example.edge_node.pojo.Image;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Create by zhangran
 */
@Repository
@Mapper
public interface ImageMapper {
    public List<Image> getImageByName(String name);
    public List<Image> getImageById(String id);
    public void saveImage(Image image);
    public void delImage(String id);
    public List<Image> getImages();
}
