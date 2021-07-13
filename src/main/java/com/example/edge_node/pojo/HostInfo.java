package com.example.edge_node.pojo;

import lombok.Data;

/**
 * Create by zhangran
 */
@Data
public class HostInfo {
    private Integer containers;
    private Integer containersStopped;
    private Integer containersPaused;
    private Integer containersRunning;
    private Integer images;
    private Long memTotal;

}
