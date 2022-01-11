package com.example.edge_node.cluster.slave;

/**
 * Create by zhangran
 */

import com.example.edge_node.cluster.codec.NettyKryoDecoder;
import com.example.edge_node.cluster.codec.NettyKryoEncoder;
import com.example.edge_node.cluster.dto.Message;
import com.example.edge_node.cluster.serialize.KryoSerializer;
import com.example.edge_node.config.MasterCondition;
import com.example.edge_node.config.SlaveCondition;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;


/**
 * @author shuang.kou
 * @createTime 2020年05月13日 20:48:00
 */
//@Conditional(SlaveCondition.class)
@Component
@Slf4j
public class NettyClient {

    private final String host;
    private final int port;
    private static final Bootstrap b;

    public NettyClient(@Value("${server.master}") String host,@Value("${server.master_port}") int port) {
        this.host = host;
        this.port = port;
    }

    // 初始化相关资源比如 EventLoopGroup, Bootstrap
    static {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        b = new Bootstrap();
        KryoSerializer kryoSerializer = new KryoSerializer();
        b.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                // 连接的超时时间，超过这个时间还是建立不上的话则代表连接失败
                //  如果 15 秒之内没有发送数据给服务端的话，就发送一次心跳请求
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        /*
                         自定义序列化编解码器
                         */
                        // RpcResponse -> ByteBuf
                        ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, Message.class));
                        //  RpcRequest -> ByteBuf
                        ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, Message.class));
                        ch.pipeline().addLast(new NettyClientHandler());
                    }
                });
    }

    /**
     * 发送消息到服务端
     *
     * @param rpcRequest 消息体
     * @return 服务端返回的数据
     */
    public void sendMessage(Message rpcRequest) {
        try {
            ChannelFuture f = b.connect(host, port).sync();
            log.debug("client connect  {}", host + ":" + port);
            Channel futureChannel = f.channel();
            log.debug("send message");
            if (futureChannel != null) {
                futureChannel.writeAndFlush(rpcRequest).addListener(future -> {
                    if (future.isSuccess()) {
                        log.info("client send message: [{}]", rpcRequest.toString());
                    } else {
                        log.error("Send failed:", future.cause());
                    }
                });
                //阻塞等待 ，直到Channel关闭
                futureChannel.closeFuture().sync();
                log.info("release client: [{}]", rpcRequest.getRequestId());
            }
        } catch (InterruptedException e) {
            log.error("occur exception when connect server:", e);
        }
    }

}
