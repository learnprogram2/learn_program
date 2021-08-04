**netty核心功能精讲, 做几个demo, 分析和心愿吗, 分析架构设计如何做到高性能的**



### 3. Netty的可实践项目: 

1. dfs的仿云盘项目
2. APP即时通信项目: IM 大量client和服务器维持长连接.

3. Netty支撑1亿客户端的消息推送





### 4. 各种中间件项目: 实战还是看源码, 理由是什么

**自研小文件系统的需要:** 替代fastDSFS, 用Java写. NIO

**自研注册中心:** Eureka停更.

**公司自研任务调度中心:** zk+微服务. 需求: 简单的从kafka到HBase

**RPC框架:** 使用netty实现的Dubbo. 看源码就好了.





### 5. 未来课程计划

1. **做好netty的实践项目**: dfs云盘, IM系统, 消息推送
2. Dobbo RPC框架源码



### 6. Java原生NIO缺陷

1. 连接异常, 网络闪断
2. 半包读写, 拆包粘包
3. 网络拥塞
4. 异常码流...

- Kafka就使用原生NIO来做的, 自己解决各种问题.



### 7. Netty优点

- 封装了底层复杂的NIO通讯细节.
- 高性能, 灵活扩展.



### 8. Netty入门程序

 API练习:

1. 创建parent和child的线程, 指定serverSocketChannel的实现类.
2. 实现一个channelHandler来负责socketChannel里面的各种事件
3. 把所有的给serverBootStrap, 她负责server启动. 然后把serverSocketChannel绑定到一个端口上.

- Java-NIO设计的很简洁. 但是netty不太好看.







## 源码1:

### 15. EventLoopGroup是线程池(parent), EventLoop是线程(child)

![image-20210603201447502](netty1-netty%E5%85%A5%E9%97%A8.assets/image-20210603201447502.png)



-  **MultithreadEventLoopGroup:** 默认core*2个线程
- **MultithreadEventExecutorGroup:** 
  - ThreadPerTaskExecutor + 默认的ThreadFactory 来创建一个children数组, 维护所有的eventExecutor. 



### 16. EventLoopGroup线程池的初始化

![image-20210603215411753](netty1-netty%E5%85%A5%E9%97%A8.assets/image-20210603215411753.png)

- NioEventLoopGroup线程池, 作为parent, 用arr存放所有的NioEventLoop(child).

- NioEventLoop是一个工作的线程, 拿着一个selector, run方法负责轮询和处理selector的select()
- 每个NioEventLoop的register(channel, key, task)负责把channel注册到自己的selector上





### 17. ServerBootstrap是个工具类, 帮助操作EventLoopGroup和EventLoop































