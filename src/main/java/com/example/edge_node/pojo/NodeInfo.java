package com.example.edge_node.pojo;

import lombok.Data;

import java.util.List;

/**
 * Create by zhangran
 */
@Data
public class NodeInfo {
    String id;
    String name;
    String type;
    String img;
    String info;
    String url;
    List<NodeInfo> children;
}
