package com.example.edge_node.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;

/**
 * Create by zhangran
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Status {

    private int number_cpus;
    private float used_memory;
    private float available_memory;
    private float memory_usage;
    private float cpu_delta;
    private float system_cpu_delta;
    private float cpu_usage;


}
