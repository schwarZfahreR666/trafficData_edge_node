package com.example.edge_node.pojo;

import cn.hutool.core.util.NumberUtil;
import lombok.Data;

import java.io.Serializable;

/**
 * Create by zhangran
 */
@Data
public class SysMonitor implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务器名称
     */
    private String computerName;

    /**
     * 服务器Ip
     */
    private String computerIp;

    /**
     * 项目路径
     */
    private String userDir;

    /**
     * 操作系统
     */
    private String osName;

    /**
     * 系统架构
     */
    private String osArch;

    /**
     * 核心数
     */
    private int cpuNum;

    /**
     * CPU总的使用率
     */
    private double cpuTotal;

    /**
     * CPU系统使用率
     */
    private double cpuSys;

    /**
     * CPU用户使用率
     */
    private double cpuUsed;

    /**
     * CPU当前等待率
     */
    private double cpuWait;

    /**
     * CPU当前空闲率
     */
    private double cpuFree;

    /**
     * 内存总量
     */
    private double memTotal;

    /**
     * 已用内存
     */
    private double memUsed;

    /**
     * 剩余内存
     */
    private double memFree;

    /**
     * 内存使用率
     */
    private double MemUsage;

    public double getCpuTotal() {
        return NumberUtil.round(NumberUtil.mul(cpuTotal, 100), 2).doubleValue();
    }

    public double getCpuSys() {
        return NumberUtil.round(NumberUtil.mul(cpuSys / cpuTotal, 100), 2).doubleValue();
    }

    public double getCpuUsed() {
        return NumberUtil.round(NumberUtil.mul(cpuUsed / cpuTotal, 100), 2).doubleValue();
    }

    public double getCpuWait() {
        return NumberUtil.round(NumberUtil.mul(cpuWait / cpuTotal, 100), 2).doubleValue();
    }

    public double getCpuFree() {
        return NumberUtil.round(NumberUtil.mul(cpuFree / cpuTotal, 100), 2).doubleValue();
    }


}
