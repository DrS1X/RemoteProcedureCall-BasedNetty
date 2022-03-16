package com.zyy.rpc.provider.server;

import com.zyy.rpc.provider.handler.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Netty的服务端
 * 启动服务端监听端口
 * springboot高级应用：DisposableBean接口
 * 当springApplication异常关闭的时候，会调用destroy清理变量
 */
@Component
public class NettyRpcServer implements DisposableBean {

    @Autowired
    NettyServerHandler nettyServerHandler;

    EventLoopGroup bossGroup = null;
    EventLoopGroup workerGroup = null;
    public void start(String host, int port) {

        try {
            //bossGroup线程组：连接事件
            bossGroup = new NioEventLoopGroup(1);

            //workGroup线程组：读写事件
            workerGroup = new NioEventLoopGroup();

            //服务端启动助手
            ServerBootstrap bootstrap = new ServerBootstrap();

            //设置bossGroup线程组和workerGroup线程组
            bootstrap.group(bossGroup, workerGroup)
                    //设置服务端通道实现为NIO
                    .channel(NioServerSocketChannel.class)
                    //通道初始化对象
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //向pipeline添加自定义业务处理handler
                            socketChannel.pipeline().addLast(new StringDecoder());
                            socketChannel.pipeline().addLast(new StringEncoder());
                            socketChannel.pipeline().addLast(nettyServerHandler);
                        }
                    });


            //启动服务端并绑定端口，同时将异步改为同步
            ChannelFuture channelFuture = bootstrap.bind(host, port).sync();
            System.out.println("==============服务端启动成功===============");
            //监听通道的关闭状态
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            if (bossGroup != null)
                bossGroup.shutdownGracefully();
            if (workerGroup != null)
                workerGroup.shutdownGracefully();

        }


    }

    @Override
    public void destroy() throws Exception {
        if (bossGroup != null)
            bossGroup.shutdownGracefully();
        if (workerGroup != null)
            workerGroup.shutdownGracefully();
    }
}
