

### 20-23. Netty创建ServerSocketChannel, 并注册到EventLoop的selector上

![image-20210604175003007](netty2-%E6%BA%90%E7%A0%81.assets/image-20210604175003007.png)

![01_Netty源码架构](netty2-%E6%BA%90%E7%A0%81.assets/01_Netty%E6%BA%90%E7%A0%81%E6%9E%B6%E6%9E%84.png)

```java
AbstractBootStrap: bind(port)方法, 要把serverSocketChannel的parentGroup里面绑定一个channel, 这个channel就是NioServerSocketChannel
// 总的步骤: 就两个: 创建注册channel, 绑定端口
private ChannelFuture doBind(final SocketAddress localAddress) {
	// 1. 创建并注册channel
    final ChannelFuture regFuture = initAndRegister();
    final Channel channel = regFuture.channel();
    if (regFuture.cause() != null) {
        return regFuture;
    }

    final ChannelPromise promise;
    if (regFuture.isDone()) {
        // 成功了, 就调用doBind0方法, 这个好像是绑定端口
        promise = channel.newPromise();
        doBind0(regFuture, channel, localAddress, promise);
    } else {
        // 注册一个listener, 来处理以后的成功或者失败.
      final PendingRegistrationPromise promise = new PendingRegistrationPromise(channel);
        regFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                Throwable cause = future.cause();
                if (cause != null) {
                    promise.setFailure(cause);
                } else {
                    promise.registered();
                    doBind0(regFuture, channel, localAddress, promise);
                }
            }
        });
        return promise;
    }

    return promise;
}
// 1. 创建并注册channel: AbstractBootStrap.initAndRegister
    final ChannelFuture initAndRegister() {
        // 1.1 创建一个NioServerSocketChannel, 
        // 	从parentGroup里面选一个EventLoop, 连带着childGroup 给这个NioServerSocketChannel传进去.
        Channel channel = createChannel();

        // 1.2 子类实现: 
        // 	-把options配置给nioServerSocketChannel配置上.
        //	-把配置的一些attribute给channel(就是一个map)配置上
        //  -如果配置了handler(就是parentEventLoop的handler), 就设置到channel的pipeline里面
        //  -把channelHandler,childOPtions和childAttrs包装一个Acceptor(用于接受请求后创建SocketChannel)配置给channel的pipeline最后一个.
        init(channel);    

        // 1.3  把channel注册到parentGroup里面的, 这里面也是遍历拿到一个EventLoop, 然后注册上去
        // FIXME: 为什么两个eventLoop不一样? 注册到EventLoop里面的selector里了
        ChannelFuture regFuture = config().group().register(channel);
        if (regFuture.cause() != null) {
            if (channel.isRegistered()) {
                channel.close();
            } else {
                channel.unsafe().closeForcibly();
            }
        }

		// 如果到了这一步还没有失败, 只有两种情况:
        // 1. 注册已经完成了, channel已经注册到selector上了, 可以使用bind和connection了.
        // 2. 如果从另一个线程尝试注册, 已经加到eventLoop的taskQueue里面了.
        return regFuture;
    }


	// 1.ServerBootStrap:在parentGroup里选一个EventLoop线程里面创建一个NioServerSocketChannel
    Channel createChannel() {
        // 从children数组里面拿一个EventLoop(遍历拿取的)
        EventLoop eventLoop = group().next();
        return channelFactory().newChannel(eventLoop, childGroup);
    }
```

### 24. ServerSocketChannel绑定到端口上

```java
// 在AbstractBootstrap里面的bind(port)方法
// 1. 上面的第一部分, 初始化NioServerSocektChannel并注册到selector上面.
// 2. 第二部: 就是这个, 如果regFuture成功了, 就bind
    private static void doBind0(
            final ChannelFuture regFuture, final Channel channel,
            final SocketAddress localAddress, final ChannelPromise promise) {

        // This method is invoked before channelRegistered() is triggered.  Give user handlers a chance to set up
        // the pipeline in its channelRegistered() implementation.
        channel.eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                if (regFuture.isSuccess()) {
                    channel.bind(localAddress, promise).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                } else {
                    promise.setFailure(regFuture.cause());
                }
            }
        });
    }
```



### 25. Netty线程如何轮询ServerSocketChannel的网络连接事件

>  select方法，大体上可以认为在一个无限循环的方法里，不停的去等待是否有新的网络事件的发生，如果有就返回

#### 怎么执行的eventLoop的run方法的? 入口在每次往eventLoop里提交任务的时候就会把线程trigger起来.

![01_Netty源码架构 (netty2-%E6%BA%90%E7%A0%81.assets/01_Netty%E6%BA%90%E7%A0%81%E6%9E%B6%E6%9E%84%20(1).png)](019~044%E8%B5%84%E6%96%99/025_Netty%E7%9A%84%E7%BA%BF%E7%A8%8B%E6%98%AF%E5%A6%82%E4%BD%95%E8%BD%AE%E8%AF%A2ServerSocketChannel%E7%9A%84%E7%BD%91%E7%BB%9C%E8%BF%9E%E6%8E%A5%E4%BA%8B%E4%BB%B6%E7%9A%84%EF%BC%9F/01_Netty%E6%BA%90%E7%A0%81%E6%9E%B6%E6%9E%84%20(1).png)

### 26. Accept连接事件如何处理

1. 在EventLoop里面select拿到ready的key的时候, 如果是OP_ACCEPT事件就把事件甩给对应的NioServerSocketChannel.
2. NioServerSocketChannel里面有一个pipeline, 它会把事件甩给pipeline
3. pipeline接收到read事件, 从head到tail传递给所有的inboundHandlerContext. 知道有人处理不往后传递了.
4. 处理好了往回写数据, 又是从tail到head传递给所有outbound的HandlerContext, 知道有人真的写了, 没人的话就给head, 最终会包装成一个对象放到一个buffer里面.
5. 调用flush, 会把buffer里面的数据写入channel.write() 这个时候就真的发出去了.



### 27. Server对建立好的client连接交给谁来轮询网络请求

交给childGroup里面的eventLoop啊, 和NioServerSocketChannel差不多, 先init NioSocketChannel, 再register到EventLoopGroup里面的channel事件上. 

- 没有bind到端口.

- 不会在pipeline里面注册一个accept事件的handler.

### 28. client发送的消息如何读取和处理-和accetp时间一样, 交给socketChannel的pipeline



### 29. 响应消息如何发送给client: pipeline->buffer->socketChannel.write



### 30. 作业: 自己看client注册到server的代码





## Client端-TODO 这部分彤哥的没讲差不太多

### 31. NettyClient如何尝试和server建立连接

### 32. NettyClient具体发出connection

### 33. client的EventLoop轮询网络连接

### 34. client发出具体请求

### 35. client接收到响应







## Netty线程模型-Reactor

### 36. 如果server端单线程的问题

单线程是来不及处理的，会导致性能很差，连接非常的不稳定.

- IO处理慢, 浪费CPU.

### 37. 线程模型进化: 单个Acceptor+多个Processor线程



### 38. 再进化: Acceptor也池化 TODO: 一个端口池化的么???

Acceptor线程进行池化，由很多个Acceptor角色的线程负责处理跟客户端连接的请求，每个Acceptor线程可以负责跟一批客户端建立连接，建立好的连接再转交给 Processor线程池，100个Acceptor线程

- bind端口的还是一个NioServerSocketChannel, 接收到Accept事件交给自己的Pipeline, 这个没改变
- **parentGroup可以多个Acceptor, 绑定多个端口(这个????是课程的意思么)**







## IO核心源码-Buffer

### 39. Netty中不止两个线程池: TODO



### 40. NettyServer端读取网络IO流程



### 41. Unsafe中的动态Buffer分配-RecvByteBufAllocator



说一下他的这个组件的RecvByteBufAllocator，动态的根据你上一次请求获取到的数据大小，动态的预估这次请求的数据大小大致是会有多少，根据预估的结果创建出来一个比较符合预估大小的一个缓冲区出来

 

他应该是负责去分配ByteBuf数据缓冲区的一个组件

 

他每次到底是分配多大的一个ByteBuf数据缓冲区呢？分布式海量小文件存储系统的时候，自定义了一套协议，kafka，每次请求过来，请求头都必修带着本次请求数据的大小，我们是根据请求头来分配ByteBuffer的

 

netty而言，他不能指望每次请求都有一个请求头，通用框架，根据每次请求的大小动态的预估下一次请求的大小，动态根据预估的大小创建对应的ByteBuf

### 42. 网络请求数据读取流程

1. 根据预估分配一个ByteBuf 

2. 根据预估分配的ByteBuf的大小创建一个原生的NIO ByteBuffer
3. 从原生SocketChannel中读取数据放入原生ByteBuffer里, 再把数据放入Netty ByteBuf里，完成一次请求数据的读取, 就搞定了



### 43. 原生ByteBuffer的缺点

1. 固定长度, 无法动态调整大小.
2. API接口有的人说不太好用.我觉得还好.



### 44. Netty为请求处理提供的良好扩展: Pipeline 挺好用的.







![01_Netty源码架构 (netty2-%E6%BA%90%E7%A0%81.assets/01_Netty%E6%BA%90%E7%A0%81%E6%9E%B6%E6%9E%84%20(7).png)](019~044%E8%B5%84%E6%96%99/044_Netty%E4%B8%BA%E8%AF%B7%E6%B1%82%E5%A4%84%E7%90%86%E6%8F%90%E4%BE%9B%E7%9A%84%E8%89%AF%E5%A5%BD%E6%89%A9%E5%B1%95%EF%BC%9A%E8%87%AA%E5%AE%9A%E4%B9%89%E4%B8%9A%E5%8A%A1%E9%80%BB%E8%BE%91%E9%93%BE%E6%9D%A1/01_Netty%E6%BA%90%E7%A0%81%E6%9E%B6%E6%9E%84%20(7).png)



