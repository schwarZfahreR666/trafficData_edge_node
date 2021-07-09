package com.example.edge_node.mapper;


import com.example.edge_node.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * Create by zhangran
 */
@Repository
@Mapper
public interface UserMapper {
    public User queryUserByName(String name);
}
