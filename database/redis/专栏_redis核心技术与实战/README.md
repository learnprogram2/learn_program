Main properties and rationales of the design
===

Redis Cluster goals
---

Redis Cluster is a distributed implementation of Redis with the following goals, in order of importance in the design:
Redis Cluster 是 redis的分布式实现, 有下面的目标:

High performance and linear scalability up to 1000 nodes. There are no proxies, asynchronous replication is used, and no merge operations are performed on values.
高性能, 线性伸缩到1000node
Acceptable degree of write safety: the system tries (in a best-effort way) to retain all the writes originating from clients connected with the majority of the master nodes. Usually there are small windows where acknowledged writes can be lost. Windows to lose acknowledged writes are larger when clients are in a minority partition.
Availability: Redis Cluster is able to survive partitions where the majority of the master nodes are reachable and there is at least one reachable slave for every master node that is no longer reachable. Moreover using replicas migration, masters no longer replicated by any slave will receive one from a master which is covered by multiple slaves.
What is described in this document is implemented in Redis 3.0 or greater.




Redis 的单线程指 Redis 的网络 IO 和键值对读写是由一个线程来完成的.
其他功能, 比如持久化, 异步删除, 集群数据同步等, 其实是由额外的线程执行的.

- Redis 内存操作+高效的数据结构;
- Redis 采用了多路复用机制, 网络 IO 操作中能并发处理请求;

	针对监听套接字的非阻塞模式: 当 Redis 调用 accept() 但一直未有连接请求到达时, 线程可以返回处理其他操作
	虽然 Redis 线程可以不用继续等待, 但是总得有机制继续在监听套接字上等待后续连接请求，并在有请求时通知 Redis.
	也需要有机制继续监听该已连接套接字，并在有数据达到时通知 Redis。




## 10.20: 阅读0405, AOF和RDB两节
Redis Server还有其他线程在后台工作，例如AOF每秒刷盘、异步关闭文件描述符这些操作。

增量RDF:

AOF重写:
在redis4.0以后，redis的 AOF 重写的时候就直接把 RDB 的内容写到 AOF 文件开头，将增量的以指令的方式Append到AOF。查看是否以 REDIS 开头的就是混合了RDB和AOF的。



## 10.21: 阅读: 数据同步, 哨兵机制

### 06. 数据同步: 主从库实现数据一致:

Redis 具有高可靠性有两层含义：一是数据尽量少丢失(AOF 和 RDB 保证), 二是服务尽量少中断(slave冗余备份保证).

主从库之间采用读写分离: 读操作：主库, 从库都可以接收; 写操作: 首先到主库执行. 然后主从同步

![Redis主从库和读写分离](https://static001.geekbang.org/resource/image/80/2f/809d6707404731f7e493b832aa573a2f.jpg)

#### b. 主从数据库第一次同步


多个Redis实例, 通过replicaof(5.0之前slaveof)命令形成主从; ![主从库首次同步](https://static001.geekbang.org/resource/image/63/a1/63d18fd41efc9635e7e9105ce1c33da1.jpg)
```shell
replicaof 172.16.19.3 6379
```

1. 从库给主库发送 psync 命令(包含runID, offset), 主库根据命令参数来启动复制. 
	- runID: redis实例的ID, 第一次问号?
	- offset: 第一次复制, -1.

2. 主库响应 FULLRESYNC命令(包含runID, 主库的offset)

3. 主库把RDB文件响应给从库.
	主库执行 bgsave 命令，生成 RDB 文件，接着将文件发给从库;
	从库收到文件, 清空DB, 加载RDB.
	
4. 主库把offset之后的写操作都写到 replication buffer.
	发送完RDB之后就开始同步了, 发送replication-buffer的数据.
	
5. 注: 也有不基于磁盘RDB的基于socket的, 边生成RDB边发送
	
#### c. 主->从->从 模式减少master压力

master在一次全量复制时候, 要先bgsave fork子进程. 如果多个slave同时同步的话, 压力很大.

![级联的“主-从-从”模式](https://static001.geekbang.org/resource/image/40/45/403c2ab725dca8d44439f8994959af45.jpg)

#### d. 基于长连接的命令传播, 重连步骤

一旦主从库完成了全量复制, 它们之间就会一直维护一个网络连接, 主库会通过这个连接将后续陆续收到的命令操作再同步给从库;

1. Redis 2.8 之前, 断连之后从库就会和主库重新进行一次全量复制
2. Redis 2.8: 主从库会**增量复制**的方式继续同步

主从断连增量复制:
1. 主从同步的时候master把写操作写入replication_buffer, 还会写入repl_backlog_buffer 环形缓冲区做一个小备份;
2. 断开的时候, replication_buffer没有了, 写命令就只能写入repli_backlog_buffer
2. 重新连接的时候, slave把自己上次的 repl_backlog_buffer offset 发给master, master响应自己的offset
3. master把两个offset之间的数据发给slave. 如果slave自己的offset被覆盖了, slave就全量复制.

1. repl_backlog_buffer：就是上面我解释到的，它是为了从库断开之后，如何找到主从差异数据而设计的环形缓冲区，从而避免全量同步带来的性能开销。
2. replication buffer：Redis和客户端通信也好，和从库通信也好，Redis都需要给分配一个 内存buffer进行数据交互，客户端是一个client，从库也是一个client，我们每个client连上Redis后，Redis都会分配一个client buffer，所有数据交互都是通过这个buffer进行的：Redis先把数据写到这个buffer中，然后再把buffer中的数据发到client socket中再通过网络发送出去，这样就完成了数据交互
client-output-buffer-limit参数限制这个buffer的大小，如果超过限制，主库会强制断开这个client的连接，也就是说从库处理慢导致主库内存buffer的积压达到限制后，主库会强制断开从库的连接，此时主从复制会中断，中断后如果从库再次发起复制请求

问题:
1. 主从库全量复制的时候是不是用这个长连接啊?
	主从库建立起的这个"命令传播的长连接", 是刚开始第一步slave发送replicaof命令的时候就建立好, 分配buffer一直维护的




### 07 | 哨兵机制：主库挂了，如何不间断服务？

哨兵机制是管理主从库自动切换实现故障转移的机制. zk集群

#### a. 哨兵机制的基本流程

哨兵主要负责的就是三个任务：监控、选主（选择主库）和通知。
![](https://static001.geekbang.org/resource/image/ef/a1/efcfa517d0f09d057be7da32a84cf2a1.jpg]
1. 监控: 周期给master, slaves发送Ping命令. 没有响应的redis标记下线.
2. 选主: 主库下线, 按照规则从slaves里面选
3. 通知: 新master的连接信息发送到slaves里, slave就会replicaof同步了.

#### b. 监控: 主观下线和客观下线

“客观下线”的标准就是，当有 N 个哨兵实例时，最好要有 N/2 + 1 个实例判断主库为“主观下线”，才能最终判定主库为“客观下线”
![客观下线的判断](https://static001.geekbang.org/resource/image/19/0d/1945703abf16ee14e2f7559873e4e60d.jpg)



#### c. 选主: 筛选 + 打分 选定新主库

![选择新主库](https://static001.geekbang.org/resource/image/f2/4c/f2e9b8830db46d959daa6a39fbf4a14c.jpg)

筛选: 各个slave的历史在线状况, 不好的就干掉.
打分: slave的 优先级, 复制进度和slaveID号.
	slave-priority参数配置salve的优先级;
	复制进度高的分数高
	slaveID越小分数越高

问题: 
1. 切换过程中，客户端能否正常地进行请求操作呢
	读正常, 写失败, 失败的时间: 从master挂掉, 到client感知到new master.
	如果要容错, 可以client先把写命令缓存起来.
	



## 10.22 

### 08 | 哨兵集群：哨兵挂了，主从库还能切换吗？

#### a. 基于 pub/sub 机制的哨兵集群

主从集群中，主库上有一个"__sentinel_:hello"的频道, 不同哨兵就是通过它来相互发现和通讯. 
哨兵和master建立连接, 就可以通过channel来发布自己的连接信息, 获取其他sentinel的连接信息.
![](https://static001.geekbang.org/resource/image/ca/b1/ca42698128aa4c8a374efbc575ea22b1.jpg)

sentinel向master周期发送INFO命令, master响应集群的从库列表, 和slave建立连接
![](https://static001.geekbang.org/resource/image/88/e0/88fdc68eb94c44efbdf7357260091de0.jpg)

#### b. 基于 pub/sub 机制的客户端事件通知
哨兵是特殊模式的redis, sentinel之间通过"__sentinel_:hello"通讯. sentinel和client之间也通过多个channel来交互事件.
![哨兵发布事件的channel](https://static001.geekbang.org/resource/image/4e/25/4e9665694a9565abbce1a63cf111f725.jpg)
client从channel里面拿到各种事件, switch-master命令会告诉新的master地址. 
问题: client刚开始怎么知道sentinel的?
答: 客户端读服务的配置文件，配置文件写了哨兵的地址和端口，然后客户端去连哨兵拿信息.


#### c. 由哪个哨兵执行主从切换？ 客观下线 和 leader选举是分开的哦.
1. 多个sentinel都判断主管下线, 多数就变成客观下线.
2. 判断master主观下线的sentinel 给其他实例发送 `is-master-down-by-addr`命令, 其他sentinel响应Y/N 
3. sentinel收到超过quota的Y之后, 就判断master客观下线了.
4. 这个sentinel想起他的sentinel发送命令, 希望自己执行主从切换. 可能有多个sentinel参与选举, 这是*Leader选举*.
5. 拿到半数赞成票, 而且这个票数大于上面的quota的sentinel成为哨兵Leader.
6. sentinelLeader负责按照打分机制把一个slave变成master. 
![](https://static001.geekbang.org/resource/image/e0/84/e0832d432c14c98066a94e0ef86af384.jpg)
![](https://static001.geekbang.org/resource/image/5f/d9/5f6ceeb9337e158cc759e23c0f375fd9.jpg)
注意: 
1. 如果只有2个sentinel, 挂掉一个无法满足过半数的要求, 所以sentinel至少3个.
2. 要保证所有sentinel配置一致, 如果down-after-milliseconds不同, 对masterdown的判断就不同, 服务不稳定.

问题: 
1. 假设有一个 Redis 集群，是“一主四从”，同时配置了包含 5 个哨兵实例的集群，quorum 值设为 2。在运行过程中，如果有 3 个哨兵实例都发生故障了，此时，Redis 主库如果有故障，还能正确地判断主库“客观下线”吗？如果可以的话，还能进行主从库自动切换吗？此外，哨兵实例是不是越多越好呢，如果同时调大 down-after-milliseconds 值，对减少误判是不是也有好处呢？
	我觉得可以完成客观下线, quorum是2,两个都认同, 就下线了.
	不能完成Leader选举, 没法达到半数.




### 09 | 切片集群：数据增多了，是该加内存还是加实例？

![切片集群架构图](https://static001.geekbang.org/resource/image/79/26/793251ca784yyf6ac37fe46389094b26.jpg)
#### a. 数据切片和实例的对应分布关系
Redis Cluster 用HashSlot 来处理数据和实例之间的映射关系. 每个key按照CRC16算出一个16bit的hash, 然后对16384(14bit)取模, 得到slot.
通过`cluster create`命令创建, slot平均分配给所有的实例. `cluster meet`命令可以手动建立集群.
![](https://static001.geekbang.org/resource/image/7d/ab/7d070c8b19730b308bfaabbe82c2f1ab.jpg)
问题: 谁算对应的slot这个呢? client么

#### b. 客户端如何定位数据？
1. redis每个实例在收到自己的slot信息, 会把它发给其他的redis, 大家都有了.
2. client连接cluster, 会收到集群的slot映射信息, 缓存起来.
3. client每次发key的时候, 计算好自己应该发给谁.
4. 如果slot有调整, client发错了, redis会重定向client, 通过MOVE响应
```text
GET hello:key(error) 
MOVED 13320 172.16.19.5:6379
# 键对应的13320slot, 应该是172.16.19.5实例.
```
![moved重定向](https://static001.geekbang.org/resource/image/35/09/350abedefcdbc39d6a8a8f1874eb0809.jpg)
5. 如果slot正在迁移给其他的redis中, 那么client会受到ask报错响应. 返回slot正在迁往的redis
6. client会给新的redis, 发送Asking空信息, 告诉他必须接受请求了.
7. client再发送给新的redis GET命令.
8. 注意: ask响应不会把client的slot映射给更新, 万一迁移失败呢.
![ask重定向](https://static001.geekbang.org/resource/image/e9/b0/e93ae7f4edf30724d58bf68yy714eeb0.jpg)

问题: 如果一个表直接放key和redis的映射, 不用CRChash计算key和slot了, 为什么不可以? 空间太大了.

总结: redis3.0之后才出, 和codis的区别就是一个是中央代理, 一个是平等的集群. 都是拿slot


























