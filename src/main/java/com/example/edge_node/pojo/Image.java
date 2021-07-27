package com.example.edge_node.pojo;

import lombok.Data;

/**
 * Create by zhangran
 */
@Data
public class Image {
    private String name;
    private String id;

    public Image(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public Image() {
    }
}
