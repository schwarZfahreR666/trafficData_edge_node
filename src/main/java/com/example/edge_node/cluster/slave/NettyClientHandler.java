package com.example.edge_node.cluster.slave;

import com.example.edge_node.cluster.dto.Message;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Create by zhangran
 */
@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            Message response = (Message) msg;
            log.debug("client receive msg: [{}]", response.toString());
            System.out.println(response);
            // 声明一个 AttributeKey 对象
            AttributeKey<Message> key = AttributeKey.valueOf("response");
            // 将服务端的返回结果保存到 AttributeMap 上，AttributeMap 可以看作是一个Channel的共享数据源
            // AttributeMap的key是AttributeKey，value是Attribute
            ctx.channel().attr(key).set(response);
            ctx.channel().close();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("client caught exception", cause);
        ctx.close();
    }
}
