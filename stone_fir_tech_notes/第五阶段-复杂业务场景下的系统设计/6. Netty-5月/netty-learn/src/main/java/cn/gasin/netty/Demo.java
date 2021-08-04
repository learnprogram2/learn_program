package cn.gasin.netty;

public class Demo {

    public static void main(String[] args) {

        new NServer().start();
        new NClient().start();

//        EventLoopGroup parentGroup = new NioEventLoopGroup(); // 线程组
//        EventLoopGroup childGroup = new NioEventLoopGroup(); // 线程组
//
//        try {
//
//            // netty服务器启动的一个启动器
//            ServerBootstrap serverBootstrap = new ServerBootstrap(); // 相当于Netty的服务器
//
//
//            serverBootstrap
//                    // 1. 设置 acceptor和client端的 event/IO处理线程池, 就是为了处理socketChannel的实践
//                    .group(parentGroup, childGroup)
//                    // 2. 设置channel的类, 到时候该用的时候就会创建
//                    .channel(NioServerSocketChannel.class)  // 监听端口的ServerSocketChannel
//                    .option(ChannelOption.SO_BACKLOG, 1024) // 这个是给channel配置一点东西
//
//                    // 3. 接收childeGroup里面的各种事件
//                    .childHandler(new ChannelInitializer<SocketChannel>() { // 处理每个连接的SocketChannel
//                        @Override
//                        protected void initChannel(SocketChannel socketChannel) throws Exception {
//                            socketChannel.pipeline().addLast(new NettyServerHandler()); // 针对网络请求的处理逻辑
//                        }
//                    });
//
//            ChannelFuture bindFuture = serverBootstrap.bind(50070).sync(); // 同步等待启动服务器监控端口
//
//            bindFuture.channel().closeFuture().sync(); // 同步等待关闭启动服务器的结果
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            parentGroup.shutdownGracefully();
//            childGroup.shutdownGracefully();
//        }
    }
}
