### 45. BIO网络通讯性能差在哪?

一个连接一个线程, CPU吃不消.



### 46. Netty使用NIO非阻塞



### 47. NIO: JDK1.4的Select, poll模式实现的NIO: 遍历寻找就绪Channel

```
Select/Poll模式
线程遍历所有的文件(linux里一切皆文件), 如果某个网络连接没有就绪(没有网络事件发生), 就对那个网络连接插入一个wait_queue节点, 然后继续遍历别的文件

如果有某个文件有就绪状态(比如有网络数据包到达), 就把这些文件的就绪状态复制给用户进程,
如果没有一个文件是就绪状态, 那么就阻塞等待唤醒.
如果某个网络连接有事件(比如网络数据包到达), 就遍历自己的wait_queue等待队列, 然后回调函数, 唤醒在阻塞等待的线程
线程被唤醒之后, 再次遍历所有文件的就绪状态, 如果有就绪就返回给用户进程

实现单个线程的多路复用, select/poll模式几乎是一致的
```

### 48. NIO: JDK1.5的Epoll模式实现:  TODO这个要了解

select/poll 模式下 selector注册了100个客户端, 一个客户端有网络事件, Selector必须重新遍历一遍100个客户端, 收集出来一个客户端的网络事件交给你的线程来进行处理.

**epoll模式: 某个文件有就绪状态, 直接回调epoll回调函数, 把就绪的文件放入epoll的一个数据结果中.** 然后epoll直接就知道哪些文件是就绪的, 不需要有一个重新遍历的过程, 所以效率更高





### 49. Netty百万连接优化-TODO

1. **多个线程监听ServerSocketChannel的连接请求, 然后多个线程负责IO读写，这是常见的模式.** - ?????????TODO这个是怎么弄的?
2. 在高配置物理机上，甚至可以单机支撑百万连接，比如64核128G的高配物理机，Acceptor线程开启100个，负责百万客户端的接入







### 50. 无锁化串行设计: 单线程的EventLoop !!!



### 51. 多线程并发优化: 减少独占锁, 尽量用volatile+CAS+AQS.

对Netty框架内部而言，主要的性能优化就是在于并发的优化，尽量避免使用synchronized等重量级锁，而是采用volatile、CAS、并发安全集合、读写锁，来尽最大可能优化多线程并发的锁争用问题





### 52. 序列化优化: 一级和二级序列化分开, 使用protobuf

1. 一次编解码: 指定长度接收数据.
2. **二次编解码: 把字符数组转化成有意义的内容** protobuf序列化性能好.



### 53.54. ByteBuf优化: Direct模式, 池化buffer

ByteBuffer，有一种特殊的Buffer，Direct模式的Buffer，创建ByteBuffer的时候可以指定是创建Direct模式的Buffer



### 55. Netty提供的参数优化: buffer相关

SO_RCVBUF和SO_SNDBUF，128kb或者256kb 

SO_TCPNODELAY，关闭这个算法，避免自动打包发送，避免高延时



### 56. Netty高可靠: 自动识别连接断开, 释放资源

CONNECT_TIMEOUT_MILLIS，可以设置连接超时时间



### 57. Netty高扩展: 空闲检测: 添加一个handler

![img](https://pic1.zhimg.com/80/v2-6a45edde5133550985fea7b6870bd480_720w.png)



### 58. 注意: NioEventLoop中处理IO异常避免线程中断



### 59. Nio epoll空轮询bug

#### **Selector BUG出现的原因**

若JDK源码里Selector的轮询结果为空，也没有wakeup或新消息处理，则发生空轮询，CPU使用率100%，

#### **Netty的解决办法**

- 对Selector的select操作周期进行统计，每完成一次空的select操作进行一次计数，
- 若在某个周期内连续发生N次空轮询，则触发了epoll死循环bug.
- 重建Selector，判断是否是其他线程发起的重建请求，若不是则将原SocketChannel从旧的Selector上去除注册，重新注册到新的Selector上，并将原来的Selector关闭。

epoll bug，它会导致Selector空轮询，最终导致CPU 100%。官方声称在JDK1.6版本的update18修复了该问题，但是直到JDK1.7版本该问题仍旧存在，只不过该BUG发生概率降低了一些而已，它并没有被根本解决。







### 60. 缓冲池中的buffer块释放, 避免内存泄漏





## 总结

### 61. Netty架构设计: 高并发, 高性能, 高可靠, 高可扩展性

 高并发架构设计：两层线程模型、NIO多路复用非阻塞、无锁串行化、并发优化

高性能架构设计：Protobuf序列化协议、direct buffer、bytebuf内存池、网络参数优化

高可靠架构设计：网络连接断开、网络链路探查、NioEventLoop线程容错、JDK epoll bug处理、内存块自动释放

可扩展架构设计：handler链条你可以自己扩展、序列化协议、定制网络通信协议



### 62. 作业: 架构设计的细节自己在源码里找出来 TODO

 高并发架构设计：两层线程模型、NIO多路复用非阻塞、无锁串行化、并发优化

高性能架构设计：Protobuf序列化协议、direct buffer、bytebuf内存池、网络参数优化

高可靠架构设计：网络连接断开、网络链路探查、NioEventLoop线程容错、JDK epoll bug处理、内存块自动释放

可扩展架构设计：handler链条你可以自己扩展、序列化协议、定制网络通信协议



























