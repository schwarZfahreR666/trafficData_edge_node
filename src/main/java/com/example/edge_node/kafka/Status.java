package com.example.edge_node.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create by zhangran
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Status {
    private int status;
    private String info;
}
