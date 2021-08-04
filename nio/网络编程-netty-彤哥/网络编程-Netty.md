### 06. Netty架构设计

![图片描述](%E7%BD%91%E7%BB%9C%E7%BC%96%E7%A8%8B-Netty.assets/5f0d60c40001334d05920348.png)

- Core，核心层，主要定义一些基础设施，比如事件模型、通信 API、缓冲区等。
- Transport Service，传输服务层，主要定义一些通信的底层能力，或者说是传输协议的支持，比如 TCP、UDP、HTTP 隧道、虚拟机管道等。
- Protocol Support，协议支持层，这里的协议比较广泛，不仅仅指编解码协议，还可以是应用层协议的编解码，比如 HTTP、WebSocket、SSL、Protobuf、文本协议、二进制协议、压缩协议、大文件传输等，基本上主流的协议都支持。

2. **模块设计**

   <img src="%E7%BD%91%E7%BB%9C%E7%BC%96%E7%A8%8B-Netty.assets/5f0d60b00001999d10340739.png" alt="模块设计" style="zoom:50%;" />

- core模块:
  - **netty-common:** 共用工具类, JDK增强(Future, FastThreadLocal), 并发包(EventExecutor..), 集合包(hashMap增强)
  - **netty-buffer:** ByteBuf, 对标JDK的ByteBuffer.
  - **netty-resolver: 解析地址**
  
- Transport模块
  
  - **netty-transport:** 一些channel, channelHandlerCOntext, eventLoop之类的. 对TCP, UDP, 还有xxx的支持
  - netty-transport-sctp, netty-transport-rxtx, netty-transport-udt: 对不同协议的支持.
  
- 协议层:

  - **netty-handler:** 类似于工具类: IP过滤, 日志, SSL...
  - **netty-codec:** 一系列编码解码器: base64, json, protobuf, seralization,... 还有主流协议的编码解码器: http, http2, mqtt, redis, stomp... 通过ChannelHandler自定义

  > netty-codec 与 netty-handler 是两个平齐的模块，并不互相依赖，没有包含和被包含的关系，ChannelHandler 接口位于 netty-transport 模块中，两者都依赖于 netty-transport 模块。

![图片描述](%E7%BD%91%E7%BB%9C%E7%BC%96%E7%A8%8B-Netty.assets/5f0d609b0001ac8215281648.png)



### 7. 优雅地编写Netty应用程序

```java
// 1. 定义线程组.
EventLoopGroup parentGroup = new NioEventLoopGroup(1); // 负责accept事件, 1个就行.
EventLoopGroup childGroup = new NioEventLoopGroup();   // CPU核心*2个.
// 2. 引导类, 集成所有配置, 负责统筹Netty程序
ServerBootstrap serverBootstrap = new ServerBootstrap(); 

// 3. 设置线程池,把线程池设置给buutStrap中.
serverBootstrap.group(parentGroup, childGroup);    
// 4. 设置ServerSocketChannel类型
serverBootstrap.channel(NioServerSocketChannel.class);
serverBootstrap.channel(EpollServerSocketChannel.class); // linux系统的高效实现.
// 5. 设置参数, 很多参数, 大多数情况下不用修改.
serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024); // 最大等待连接数量
// 6. (可选)设置serverSocketChannel的handler, 只能设置一个, 在SocketChannel建立前执行
serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));
// 7. 设置子handler(必须), 两种handler: inBound和outBound的handler.
serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();
		// 可以添加多个子Handler
		p.addLast(new LoggingHandler(LogLevel.INFO));
		p.addLast(new EchoServerHandler()); // 可以是inBound也可以是outBound
	}
});
// 8. 绑定端口
ChannelFuture channelFuture = serverBootstrap.bind(8080).sync(); // 同步
// 9. 等待服务端口关闭(必须)
channelFuture.channel().closeFuture().sync();

// 10. finally中优雅关闭线程池:
parentGroup.shutdownGracefully();
childGroup.shutdownGracefully();

```

**为什么需要设置 ServerSocketChannel 的类型，而不需要设置 SocketChannel 的类型呢？**

那是因为 SocketChannel 是 ServerSocketChannel 在接受连接之后创建出来的，所以，并不需要单独再设置它的类型，比如，NioServerSocketChannel 创建出来的肯定是 NioSocketChannel，而 EpollServerSocketChannel 创建出来的肯定是 EpollSocketChannel。

#### 子handler: 拿到socketChannel就是一个和client的socket连接







### 08. Netty的10个核心组件

1. **Bootstrap和ServerBootstrap**

   Netty程序的引导类, 配置各种参数, 启动Netty-server或者client. 两者都继承AbstractBootstrap.

   不同点: ServerBootstrap要处理accept事件.

2. **EventLoopGroup:** 线程池

   继承顺序: 

   1. EventExecutorGroup(扩展ScheduledExecutorService, 提供next()遍历EventExecutor, 管理EventExecutor的生命周期)

   2. EventLoopGroup(扩展executorGroup, 提供next()来遍历eventLoop, 提供注册channel到事件轮询器)

   3. MultithreadEventLoopGroup(抽象类, 模板) 

   4. NioEventLoopGroup(具体实现类), 类似的实现类还有EpollEventLoopGroup 专门用于 Linux 平台, KQueueEventLoopGroup 专门用于 MacOS/BSD 平台

      select/epoll/kqueue, epoll 和 kqueue 比 select 更高效, epoll只支持Linux

3. **EventLoop:** 线程池里的工作线程

   继承顺序: 

   1. EventExecutor: 扩展EventLoopGroup, 可以判断一个线程是不是在EventLoop中.
   2. OrderedEventExecutor: 扩展EventExecutor, 标记里面的任务按顺序执行
   3. EventLoop: 扩展EventLoopGroup, 为自己的selector上面注册的Channel处理IO事件. 按照Ordered里面的顺序
   4. SingleThreadEventLoop: 抽象类, 是一个单线程的模板
   5. NioEventLKoop: 实现类, 绑定到一个selector, 接受很多channel.![图片描述](%E7%BD%91%E7%BB%9C%E7%BC%96%E7%A8%8B-Netty.assets/5f0d64590001e71110300611.png)

4. **ByteBuf** 在JavaNIO的Buffer之上创建新的模式的Buffer

   一个readIndex用于读取数据, 一个writeIndex用于写数据.![图片描述](%E7%BD%91%E7%BB%9C%E7%BC%96%E7%A8%8B-Netty.assets/5f0d644700016c2e10270217.png)

   ByteBuf分三种维度, 有2* 2 * 2 8种可能:, 使用ByteBufAllocator 来创建合适的.

   - Pooled和Unpooled: 

     **池化:** 初始化一块内存作为内存池, 每次创建ByteBuf的时候从内存池中分配一块连续内存给ByteBuf使用. 可减少VM的频繁内存税收的性能消耗

     **非池化:** 利用JVM本身的内存管理来分配

   - **Heap和Direct**

     堆内存: JVM堆内分配

     直接内存: 向操作系统申请一块(用户空间)内存

   - **Safe和Unsafe**

     Unsafe, 底层使用 Java 本身的 Unsafe 来操作底层的数据结构, 即直接利用对象在内存中的指针来操作对象.

   ![图片描述](%E7%BD%91%E7%BB%9C%E7%BC%96%E7%A8%8B-Netty.assets/5f0d641a0001bf4113780487.png)

5. **Channel:** JavaNIO的封装
   - 通过 ChannelPipeline 来处理 IO 事件
   - 异步IO
   - JavaNIO-Channel 对应Netty 的 Channel 都有相应的包装类，并且还扩展了其它协议的实现: ![图片描述](%E7%BD%91%E7%BB%9C%E7%BC%96%E7%A8%8B-Netty.assets/5f0d619500013e6b11270187.png)

6. **ChannelHandler:** 业务处理接口, 处理或者拦截IO事件.多个channelHandler组成channelPipeline.

   分为出入站两类: channelInboundHandler, channelOutboundHGandler. 实现两个抽象类

   - **SimpleChannelInboundHandler**: 处理入站事件
   - **ChannelOutboundHandlerAdapter:** 处理出站事件
   - **ChannelDuplexHandler:** 双向

7. **ChannelHandlerContext:** 

   保存着Channel的上下文, 关联到一个ChannelHandler. ChannelHandler通过这个context才能和ChannelPipeline和其他的channelHandler交互.

8. **ChannelFuture:** 

   IO都是一部的, 返回ChannelFuture. 可以查看IO操作是否完成, 一场是什么

9. **ChannelPipeline:**

   是channelHandler的链表, 负责处理出入站的事件. 存储的是ChannelHandlerContext链.

   - 一个 Channel 对应一个 ChannelPipeline
   - 一个 ChannelPipeline 包含一条双向的 ChannelHandlerContext 链
   - 一个 ChannelHandlerContext 中包含一个 ChannelHandler
   - 一个 Channel 会绑定到一个 EventLoop 上
   - 一个 NioEventLoop 维护了一个 Selector（使用的是 Java 原生的 Selector）
   - 一个 NioEventLoop 相当于一个线程

   ![图片描述](%E7%BD%91%E7%BB%9C%E7%BC%96%E7%A8%8B-Netty.assets/5f0d6180000114a212880566.png)

10. **ChannelOption:** 保存很多channel的配置参数.

![图片描述](%E7%BD%91%E7%BB%9C%E7%BC%96%E7%A8%8B-Netty.assets/5f0d616c00013bc015351093.png)





### 09. Reactor模式: 为什么ServerBootstrap需要两个线程池

1. Reactor 模式是一种事件处理模式, 有一个或多个输入源(inputs)
2. 用于处理服务请求，把它们并发地传递给一个服务处理器(service handler)
3. 服务处理器将这些请求以多路复用的方式分离（demultiplexes ），并把它们同步地分发到相关的请求处理器(request handlers);

![图片描述](%E7%BD%91%E7%BB%9C%E7%BC%96%E7%A8%8B-Netty.assets/5f0d67e00001022910320325.png)



**变异的主从Reactor模型**

![图片描述](%E7%BD%91%E7%BB%9C%E7%BC%96%E7%A8%8B-Netty.assets/5f0d65aa00019eb110320529.png)

![图片描述](%E7%BD%91%E7%BB%9C%E7%BC%96%E7%A8%8B-Netty.assets/5f0d64e50001801112681011.png)





### 10. 如何解决粘包, 拆包问题

1. **粘包原因:**

   TCP发送消息有缓冲区, **当消息内容远小于缓冲区, 会和其它消息合并后再发出去**

   TCP接收消息也有缓冲区, **当接收读取不及时, 也会粘包**

2. **拆包原因**

   消息太大, 超过了缓冲区.

   消息在协议各层都有用, 如果超过了当前协议层的MTU(maximum transmission unit)最大数据量的时候会拆包

   ![图片描述](%E7%BD%91%E7%BB%9C%E7%BC%96%E7%A8%8B-Netty.assets/5f0d71530001d08c05290154.png)

3. **本质原因: TCP是流式协议, 消息不会有边界**

4. UDP不会有, 因为她的消息有明确边界.

**解决办法:** 定长法, 分隔符法, 长度+内容法.

1. **定长法:** 固定长度来确定消息的边界. 不足就补充到. 回复浪费IO资源

2. **分隔符法:** 使用固定的分割符分割消息, 比如传输的消息分别为 ABC, DEFG, HI\n, 假如使用 \n 作为分割符.

   但是要扫描消息全部内容才能确定消息边界.

   比如JSON协议, 找到{}来分割.

3. **长度+内容法:** 自定义通讯的格式, 用长度来限制消息包长度.

| 方法          | Netty的编码          | Netty的解码                  |
| :------------ | :------------------- | :--------------------------- |
| 定长法        | 无                   | FixedLengthFrameDecoder      |
| 分割符法      | 无                   | DelimiterBasedFrameDecoder   |
| 长度 + 内容法 | LengthFieldPrepender | LengthFieldBasedFrameDecoder |

```java
public final class EchoServer {
    // 使用
    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));

    public static void main(String[] args) throws Exception {
        // 省略其它代码
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new LoggingHandler(LogLevel.INFO));
                            // 添加固定长度解码器，长度为3
                            p.addLast(new FixedLengthFrameDecoder(3));
                            p.addLast(echoServerHandler);
                        }
                    });
        // 省略其它代码
    }
}
```

![图片描述](%E7%BD%91%E7%BB%9C%E7%BC%96%E7%A8%8B-Netty.assets/5f0d710a00015e0e12671153.png)



### 11. Netty对常见编解码方式的支持-一次与二次编解码

**一次编解码和二次编解码是什么:**

- 一次编解码是为了防止粘包和拆包, 把缓冲区的字节数组分割清楚, **拿到我们想要的字节数组**
- **二次解码是把分割好的字节数组转换成有意义的东西**->对象/JSON/...

**一次编解码和二次编解码可以合并吗？**

可以, 但不建议. 编程的分层思想. 如果以后修改也好.

**Netty的一二次编码**

- 一次编解码：MessageToByteEncoder/ByteToMessageDecoder
- 二次编解码：MessageToMessageEncoder/MessageToMessageDecoder

![图片描述](%E7%BD%91%E7%BB%9C%E7%BC%96%E7%A8%8B-Netty.assets/5f179e780001aa4710310142.png)

| 序列化方式                            | 优点                                 | 缺点                                 |
| :------------------------------------ | :----------------------------------- | :----------------------------------- |
| serialization（优化过的 Java 序列化） | Java 原生，使用方便                  | 报文太大，不便于阅读，只能 Java 使用 |
| json                                  | 结构清晰，便于阅读，效率较高，跨语言 | 报文较大                             |
| protobuf                              | 使用方便，效率很高，报文很小，跨语言 | 不便于阅读                           |

![图片描述](%E7%BD%91%E7%BB%9C%E7%BC%96%E7%A8%8B-Netty.assets/5f179e9c0001530116400828.png)

















