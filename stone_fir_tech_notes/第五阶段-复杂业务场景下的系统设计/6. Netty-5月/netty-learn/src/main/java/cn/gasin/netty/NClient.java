package cn.gasin.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * netty实现的客户端.
 */
public class NClient {

    EventLoopGroup group;
    Bootstrap bootstrap;


    public NClient() {
        bootstrap = new Bootstrap();
        group = new NioEventLoopGroup();
    }

    public void start() {
        try {
            ChannelFuture channelFuture = bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(new NClientHandler());
                        }
                    }).connect("localhost", 50070).sync();

            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            group.shutdownGracefully();
        }
    }
}
