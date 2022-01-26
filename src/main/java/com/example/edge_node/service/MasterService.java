package com.example.edge_node.service;

import com.example.edge_node.cluster.ds.HealthMap;
import com.example.edge_node.cluster.dto.Message;

import com.example.edge_node.config.MasterCondition;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;



/**
 * Create by zhangran
 */
@Conditional(MasterCondition.class)
@Service
@Slf4j
public class MasterService {
    private static HealthMap CTX_MAP = new HealthMap();

    public static void putSlave(String slaveId,double healthScore,ChannelHandlerContext ctx){
        CTX_MAP.put(slaveId,ctx,healthScore);
    }

    public void sendMessage2slave(String slaveId, Message message){
        ChannelHandlerContext ctx = CTX_MAP.get(slaveId);
        if(ctx != null){
            ChannelFuture f = ctx.writeAndFlush(message);
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    public boolean sendMessage2health(Message message){
        ChannelHandlerContext ctx = CTX_MAP.getHealthySlave();
        if(ctx != null){
            ChannelFuture f = ctx.writeAndFlush(message);
            f.addListener(ChannelFutureListener.CLOSE);
            log.info("向slave发送消息：" + message);
        }else{
            return false;
        }
        return true;
    }
}
