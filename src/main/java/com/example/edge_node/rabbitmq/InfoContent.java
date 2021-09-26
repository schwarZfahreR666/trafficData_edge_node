package com.example.edge_node.rabbitmq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create by zhangran
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfoContent {
    String id;
    Object content;
}
