package com.example.edge_node.cluster.dto;

import lombok.*;

/**
 * Create by zhangran
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class Message {
    //rpc message type
    private double healthScore;
    //request id
    private String requestId;
    private String taskName;
}
