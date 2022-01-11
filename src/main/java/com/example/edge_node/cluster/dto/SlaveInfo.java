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
public class SlaveInfo {
    private String id;
    private double healthScore;
}
