package com.example.edge_node;

import io.netty.channel.ChannelHandlerContext;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Create by zhangran
 */
public class HealthMapTest {
    //slaveId to channel
    private static Map<String, String> ID_TO_CTX = new ConcurrentHashMap<>();
    // slaveId to health
    private static Map<String, Double> ID_TO_HEALTH = new ConcurrentHashMap<>();
    // health to ids
    private static Map<Double, LinkedHashSet<String>> HEALTH_TO_IDS = new ConcurrentHashMap<>();

    private static PriorityBlockingQueue<Double> MAX_HEALTH = new PriorityBlockingQueue<>(10,(a,b)->(int) (b - a));




    public synchronized String get(String slaveId){
        String res = ID_TO_CTX.remove(slaveId);
        return res;
    }
    public synchronized String getHealthySlave() {

        double maxHealth = 0;
        if(MAX_HEALTH.isEmpty()) return null;
        maxHealth = MAX_HEALTH.poll();
        LinkedHashSet<String> ids = HEALTH_TO_IDS.get(maxHealth);
        String slaveId = ids.iterator().next();
        ids.remove(slaveId);
        if (ids.isEmpty()) {
            HEALTH_TO_IDS.remove(maxHealth);
        }

            return get(slaveId);

    }

    public synchronized void put(String slaveId, String ctx,double healthScore){
        //已存在该slaveId

        if(ID_TO_CTX.containsKey(slaveId)){
            //关闭之前的通道，ID_TO_CTX不存在该id结果了
            ID_TO_CTX.remove(slaveId);
            //取出之前的健康度，并清除出列表，ID_TO_HEALTH和HEALTH_TO_IDS都不存在该id了
            double preHealth = ID_TO_HEALTH.remove(slaveId);
            LinkedHashSet<String> ids = HEALTH_TO_IDS.get(preHealth);
            ids.remove(slaveId);
            if(ids.isEmpty()){
                HEALTH_TO_IDS.remove(preHealth);
            }
            //清除原健康度
            MAX_HEALTH.remove(preHealth);
        }
        ID_TO_CTX.put(slaveId,ctx);
        ID_TO_HEALTH.put(slaveId,healthScore);
        HEALTH_TO_IDS.putIfAbsent(healthScore,new LinkedHashSet<>());
        HEALTH_TO_IDS.get(healthScore).add(slaveId);
        MAX_HEALTH.offer(healthScore);

    }

}
