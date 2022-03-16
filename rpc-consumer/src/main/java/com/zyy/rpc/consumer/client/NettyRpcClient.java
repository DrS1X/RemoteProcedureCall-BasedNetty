package com.zyy.rpc.consumer.client;

import com.zyy.rpc.consumer.handler.NettyRpcClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Netty 客户端
 * 1.连接服务器
 * 2.关闭资源
 * 3.提供发送消息的方法
 */
@Component
public class NettyRpcClient implements InitializingBean, DisposableBean {
    EventLoopGroup group = null;
    Channel channel = null;

    @Autowired
    NettyRpcClientHandler nettyRpcClientHandler;

    ExecutorService service = Executors.newCachedThreadPool();

    /**
     * 1.连接服务器
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        try {
            //创建线程组
            group = new NioEventLoopGroup();

            //创建客户端启动助手
            Bootstrap bootstrap = new Bootstrap();

            //设置擦书
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new StringEncoder());
                            ch.pipeline().addLast(nettyRpcClientHandler);
                        }
                    });

            //连接服务端
            channel = bootstrap.connect("127.0.0.1", 8899).sync().channel();
        } catch (Exception e) {
            e.printStackTrace();
            if (channel != null)
                channel.close();
            if (group != null)
                group.shutdownGracefully();
        }
    }

    @Override
    public void destroy() throws Exception {
        if (channel != null)
            channel.close();
        if (group != null)
            group.shutdownGracefully();
    }

    /**
     * 消息发送
     * @param msg
     * @return
     */
    public Object send(String msg) throws ExecutionException, InterruptedException {
        nettyRpcClientHandler.setReqMsg(msg);
        Future future = service.submit(nettyRpcClientHandler);
        return future.get();
    }
}
