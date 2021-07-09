package com.example.edge_node.service;


import com.example.edge_node.mapper.UserMapper;
import com.example.edge_node.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create by zhangran
 */
@Service
public class UserService {
    @Autowired
    UserMapper userMapper;
    public User queryUserByName(String name){
        return userMapper.queryUserByName(name);
    }
}
