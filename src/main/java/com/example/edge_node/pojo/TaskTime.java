package com.example.edge_node.pojo;

import lombok.Data;

import java.util.Date;

/**
 * Create by zhangran
 */
@Data
public class TaskTime {
    int freq;
    long lastTime;

    public TaskTime(int freq, long lastTime) {
        this.freq = freq;
        this.lastTime = lastTime;
    }
}
