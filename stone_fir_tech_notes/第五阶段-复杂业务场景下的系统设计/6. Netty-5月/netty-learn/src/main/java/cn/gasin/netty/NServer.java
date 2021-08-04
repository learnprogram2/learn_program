package cn.gasin.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 使用netty实现一个server
 */
public class NServer {
    // 线程组, 负责处理serverSocketChannel的事件.
    EventLoopGroup parentGroup = null;
    EventLoopGroup childGroup = null;
    ServerBootstrap serverBootstrap = null;

    public NServer() {
        this.parentGroup = new NioEventLoopGroup();
        this.childGroup = new NioEventLoopGroup();
        this.serverBootstrap = new ServerBootstrap();
    }

    public void start() {
        try {
            ChannelFuture channelFuture =
                    // 1. 存起来两个eventLoopGroup, 分别处理bind到port的socket的连接请求, 和client对接的socket的socket.
                    serverBootstrap.group(parentGroup, childGroup)
                            // 2. 创建parentGroup里面channel的时候应该用的类, 包装一个channelFactory
                            .channel(NioServerSocketChannel.class)
                            // 3. 给要创建的channel配置一些东西, 都存在options里面
                            .option(ChannelOption.SO_BACKLOG, 1024)
                            // 4. channel的数据要自定义处理
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    ch.pipeline().addLast(new NettyServerHandler());
                                }
                            })
                            // 5.
                            .bind(50070).sync();
            channelFuture.channel().closeFuture().sync();


        } catch (InterruptedException e) {
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
        }
    }


}
