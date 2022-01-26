package com.example.edge_node.cluster.master;

import com.example.edge_node.cluster.dto.Message;
import com.example.edge_node.config.MasterCondition;
import com.example.edge_node.service.MasterService;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Create by zhangran
 */
@Conditional(MasterCondition.class)
@Component
@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {


    private static final AtomicInteger atomicInteger = new AtomicInteger(1);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            Message request = (Message) msg;
            log.debug("server receive msg: [{}] ,times:[{}]", request, atomicInteger.getAndIncrement());
            System.out.println(msg);
            MasterService.putSlave(request.getRequestId(),request.getHealthScore(),ctx);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server catch exception",cause);
        ctx.close();
    }
}
