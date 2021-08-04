package cn.gasin.netty.course;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {
	
	public static void main(String[] args) {
		EventLoopGroup parentGroup = new NioEventLoopGroup(); // 线程组 -> Acceptor线程
		EventLoopGroup childGroup = new NioEventLoopGroup(); // 线程组 -> Processor / Handler
		
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap(); // 相当于Netty的服务器
			
			serverBootstrap
					.group(parentGroup, childGroup)
					.channel(NioServerSocketChannel.class)  // 监听端口的ServerSocketChannel
					.option(ChannelOption.SO_BACKLOG, 1024)
					.childHandler(new ChannelInitializer<SocketChannel>() { // 处理每个连接的SocketChannel

						@Override
						protected void initChannel(SocketChannel socketChannel) throws Exception {
							socketChannel.pipeline().addLast(new NettyServerHandler()); // 针对网络请求的处理逻辑
						}
						
					});
			
			ChannelFuture channelFuture = serverBootstrap.bind(50070).sync(); // 同步等待启动服务器监控端口
			
			channelFuture.channel().closeFuture().sync(); // 同步等待关闭启动服务器的结果
		} catch (Exception e) {
			e.printStackTrace();  
		} finally {
			parentGroup.shutdownGracefully();
			childGroup.shutdownGracefully();
		}
	}
	
}
