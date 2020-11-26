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
5. 如果slot正在迁移给其他的redis中, 那么, 没有查到的话, client会受到ask报错响应. 返回slot正在迁往的redis
6. client会给新的redis, 发送Asking空信息, 告诉他必须接受请求了.
7. client再发送给新的redis GET命令.
8. 注意: ask响应不会把client的slot映射给更新, 万一迁移失败呢.
![ask重定向](https://static001.geekbang.org/resource/image/e9/b0/e93ae7f4edf30724d58bf68yy714eeb0.jpg)

asking重定向, 这个时候是slot在**渐进式迁移**的时候: 


问题: 如果一个表直接放key和redis的映射, 不用CRChash计算key和slot了, 为什么不可以? 空间太大了.

总结: redis3.0之后才出, 和codis的区别就是一个是中央代理, 一个是平等的集群. 都是拿slot



## 10.23 周五 0.6个, 10.27 周二 2.4个

### 11 | “万金油”的String，为什么不好用了？

#### a. String内存消耗大:
1. 实际数据不一定完美的装下, 比如string装id, 实际就是64byte装8byte.
2. 还要有元数据.
![SDS](https://static001.geekbang.org/resource/image/37/57/37c6a8d5abd65906368e7c4a6b938657.jpg)
![RedisObject](https://static001.geekbang.org/resource/image/34/57/3409948e9d3e8aa5cd7cafb9b66c2857.jpg)
1. buf装了实际数据
2. len和alloc是维护的元数据, 还有包装SDS的robj的开销.
3. redisObject包含元数据和一个指针, 各8个byte.

robj保存string的三种编码:
![string三种编码](https://static001.geekbang.org/resource/image/ce/e3/ce83d1346c9642fdbbf5ffbe701bfbe3.jpg)
1. 保存long整数, 指针就直接变成一个整数, 不指了.
2. 如果是字符串,小于44byte, 就把指针和SDS连接起来存储, embstr编码方式.
3. 字符串大于44byte, 就单独放了, raw编码

![hashtable的dictEntry](https://static001.geekbang.org/resource/image/a6/8a/a6708594a86d2a49107f8b6cfc1a2b8a.jpg)
三个指针只有 24 字节, jemalloc分配内存时候会分配 32 字节: 根据申请的字节数 24, 找一个比 24 大, 但是最接近 24 的 2 的幂次数作为分配的空间.


#### b. 用什么数据结构可以节省内存？
![ziplist](https://static001.geekbang.org/resource/image/f6/9f/f6d4df5f7d6e80de29e2c6446b02429f.jpg)
一系列连续的entry保存数据, 这个可以看另一个专栏.

#### c. hash的二级编码保存单值
把单值拆分成2部分, 前一部分作为hash的key, 后一部分作为value
Hash 的存储可以用压缩列表和hash表(字典). 按照阈值选择:hash-max-ziplist-entries kv数, hash-max-ziplist-value value长度限制.
所以利用ziplist存储的优势, 可以存储作者的例子: 
把图片ID的前7位(1101000)作为Hash对象的key, 把图片ID的最后3位(060)和图片存储对象ID分别作为 Hash 类型值中的 key 和 value. 保证不超过1000, 使用ziplist存储.


### 12 | 有一亿个keys要统计，应该用哪种集合？

集合类型常见的四种统计模式: 包括聚合统计、排序统计、二值状态统计和基数统计

#### a. 聚合统计
多个集合取并交集等操作. 适合用set做. 可以在从库里做这种复杂计算.
集群模式下key分布不均匀, 可能无法做聚合计算

#### b. 排序统计
比如留言排名等需要排序的场景, 适合用soreted set.
list排序会出现分页时候又插入的问题. s_set有zrangebyscore命令. 适合分页显示和更新频繁的数据.

#### c. 二值状态统计
*Bitmap* 本身是用 String 类型作为底层数据结构实现的一种统计二值状态的数据类型

#### d. 基数统计
统计一个集合中不重复的元素个数. set: 存储原值. *HyperLogLog*: 概率统计


![几种统计方法](https://static001.geekbang.org/resource/image/c0/6e/c0bb35d0d91a62ef4ca1bd939a9b136e.jpg)


### 13 | GEO是什么？还可以定义新的数据类型吗？
Redis 除了五种基本类型, 还提供了 3 种扩展数据类型，分别是 Bitmap、HyperLogLog 和 GEO.

#### a.GEO底层结构
使用Sorted_set实现, 利用了set操作, 和基于score的范围查询.
坐标作为score, 使用GeoHash编码, 把二维进行二分, 转化一维. 前面位数相同的越多, 说明两个obj越相邻.

#### b. 如何自定义数据类型？
![redisObject](https://static001.geekbang.org/resource/image/05/af/05c2d546e507d8a863c002e2173c71af.jpg)
RedisObject 的包括了 type,encoding,lru 和 refcount 4 个元数据，以及 1 个*ptr指针。
![创建新数据类型](https://static001.geekbang.org/resource/image/88/99/88702464f8bc80ea11b26ab157926199.jpg)
为新数据类型定义好它的底层结构、type 和 encoding 属性值，然后再实现新数据类型的创建、释放函数和基本命令。
1. newtype.h 文件来保存这个新类型的定义
2. 在server.h里面头里 RedisObject 的 type 属性中，增加这个新类型的定义 #define OBJ_STRING 0 /* String object. */
3. object.c 里面开发新数据类型的create和delete接口. 实现新建一个 t_xxx.c 文件里
4. 在t_xxx.c里面开发新类型的命令操作, 然后再server.h里面声明, serveri.c里面的redisCommandTable把命令string和方法关联起来







## 10.28 周三

### 14 | 如何在Redis中保存时间序列数据？

#### a. 时间序列数据的读写特点
1. 插入数据快, 复杂度要低.
2. 查询, 单条记录, 还需要时间范围查询, 聚合查询
所以可以基于 Hash 和 Sorted Set 实现.

#### b. 基于 Hash 和 Sorted Set 保存时间序列数据
Sorted Set 只支持范围查询，无法直接进行聚合计算
RedisTimeSeries 这个第三方扩展可以实现.

问题: zset的dict里面k-v分别是什么??? 是分数-value, 还是value-分数
	应该是value, 因为如果是分数, skiplist就可以办到logn的复杂度了, 只是exist判断有没有value的时候是O(n).

### 15 | 消息队列的考验：Redis有哪些解决方案？
消息队列在存取消息时, 必须要满足三个需求, 分别是消息保序, 处理重复的消息和保证消息可靠性。
解决方案: 
![list的dr](https://static001.geekbang.org/resource/image/50/3d/5045395da08317b546aab7eb698d013d.jpg)
1. 基于 List 的消息队列解决方案
	消息顺序保证, 需要自己设计唯一ID预防重复发送, BRPOPLPUSH命令做消息ACK;
	- 不支持多个消费者的消费组概念.
2. 基于 Streams 的消息队列解决方案
	模仿Kafka的消息队列, 顺序可以保证, 有自带的唯一ID
	- 支持group
![List和Stream作为消息队列的对比](https://static001.geekbang.org/resource/image/b2/14/b2d6581e43f573da6218e790bb8c6814.jpg)
redis的消息队列适用于消息量并不是非常巨大, 数据不是非常重要.



## 10.29 thursday

### 16 | 异步机制：如何避免单线程模型的阻塞？

影响Redis性能的五大因素:
1. Redis的阻塞式操作
2. CPU核和NUMA架构
3. Redis关键系统配置
4. Redis 内存碎片
5. Redis buffer

#### a. Redis的阻塞式操作
![](https://static001.geekbang.org/resource/image/6c/22/6ce8abb76b3464afe1c4cb3bbe426922.jpg)
- 客户端: 网络IO, 键值对修改
	IO多路复用. 
	- 复杂度高的请求会阻塞: 集合全量查询和聚合
	- 删除大对象. 清空DB: 维护空闲内存块链表
- 磁盘: RDB, AOF, AOF重写
	fork子进程执行RDB, AOF重写.
	- AOF不同的写盘策略会影响
- 主从: 主库传输RDB, 从库清空DB, 加载RDB
	传输和清空DB上面都说了解决和影响
	- 加载RDB会影响
- cluster: slot信息互相传输, 数据迁移
	信息传递数据量不大, **渐进式数据迁移**, 但如果bigkey还会阻塞.
	
；bigkey ；AOF 日志同步写；从库加载 RDB 文件。

#### b. 哪些阻塞点可以异步执行？
- 集合全量查询和聚合操作: client在等着执行完毕, 不能异步. 可以scan命令分批聚合.
- 加载RDB: 必须加载完数据才能工作, 不能异步.
- bigkey删除, 清空数据库: 子线程异步删除.
- AOF: 子线程来执行 AOF 日志的同步写.

#### c. 异步的子线程机制
![redis异步线程执行任务队列](https://static001.geekbang.org/resource/image/ae/69/ae004728bfe6d3771c7424e4161e7969.jpg)
- Redis 主线程启动后，会使用操作系统提供的 pthread_create 函数创建 3 个子线程
- bigkey删除和DB清空一般是同步的,lazy-free需要手动开启, 异步化需要用"UNLINK"和"FLUSH ASYNC"命令: redis4.0之前的可以scan查到一部分然后删掉.
	Hash/Set/ZSet/List在集合元素数量多的时候才会lazy-free



### 17 | 为什么CPU结构也会影响Redis的性能？
#### a. 主流的 CPU 架构
![](https://static001.geekbang.org/resource/image/d9/09/d9689a38cbe67c3008d8ba99663c2f09.jpg)
![服务器的多CPU socket架构](https://static001.geekbang.org/resource/image/5c/3d/5ceb2ab6f61c064284c8f8811431bc3d.jpg)
Redis 单线程可以先在 Socket 1 上运行一段时间，然后再被调度到 Socket 2 上运行. 这就会丧失三级缓存的优势. L1,L2尤快
多CPU架构下, 切换CPU执行, 就是非统一内存访问架构（Non-Uniform Memory Access，NUMA 架构)

#### b. CPU多核对Redis性能影响
在一个 CPU 核上运行时，应用程序需要记录自身使用的软硬件资源信息（例如栈指针、CPU 核的寄存器值等），我们把这些信息称为运行时信息。同时，应用程序访问最频繁的指令和数据还会被缓存到 L1、L2 缓存上，以便提升执行速度。
但是，在多核 CPU 的场景下，一旦应用程序需要在一个新的 CPU 核上运行，那么，运行时信息就需要重新加载到新的 CPU 核上。而且，新的 CPU 核的 L1、L2 缓存也需要重新加载数据和指令，这会导致程序的运行时间增加。
context switch 是指线程的上下文切换: 太多就会L1,L2缓存重刷.

taskset 命令把一个程序绑定在一个核上运行。
![](https://static001.geekbang.org/resource/image/30/b0/30cd42yy86debc0eb6e7c5b069533ab0.jpg)
![](https://static001.geekbang.org/resource/image/41/79/41f02b2afb08ec54249680e8cac30179.jpg)
把网络中断程序和 Redis 实例绑在同一个 CPU Socket, 可以避免 Redis 跨 CPU Socket 访问网络数据.

TODO: ...


## 10.30 Friday (0), 11.2 Monday (2)

### 18 | 波动的响应延迟：如何应对变慢的Redis？（上）
除了上面两讲里面Redis变慢的因素, 还有一些其它因素. 这两节讲如何应对Redis.

#### a. redis是不是真的变慢了
- redis延迟. `./redis-cli –intrinsic-latency 10`, 命令查看10秒内的最大延迟.
- 平时无压力的延迟可以作为Redis的基线性能. 如果运行延迟2倍基线性能的时候压力就大了.
#### b. 如何应对 Redis 变慢？
![Redis 架构图](https://static001.geekbang.org/resource/image/cd/06/cd026801924e197f5c79828c368cd706.jpg)
红色的三块就是影响Redis性能的部分.

#### c. Redis 自身操作特性的影响
1. 慢查询: latency monitor工具, 找到慢查询. 确认复杂度是不是慢查询. 修改.
2. 过期key操作:
	检查业务代码在使用 EXPIREAT 命令设置 key的过期时间是不是一样的.

问题: KEYS和scan命令; scan命令会不会因为rehash丢数据?
	不会, hash扩容, 使用高位进位法进行遍历, 不会漏. hash缩容: 可能会映射到新hash上没有遍历到的位置, 会重复. 

#### d. 文件系统：AOF 模式
![](https://static001.geekbang.org/resource/image/9f/a4/9f1316094001ca64c8dfca37c2c49ea4.jpg)
evrysec模式下如果上一次的fsync没有完成, 会阻塞主线程. 如果阻塞了要适当调整级别, 使用固态硬盘.
![](https://static001.geekbang.org/resource/image/2a/a6/2a47b3f6fd7beaf466a675777ebd28a6.jpg)

#### e. 操作系统：swap
内存 swap 是操作系统里将内存数据在内存和磁盘间来回换入和换出的机制，涉及到磁盘的读写; 
一旦Swap触发, redis的内存操作, 要等到磁盘数据读写完成.
*物理内存不足就会触发Swap:* 检查redis进程/其他进程
可以在/proc目录下查看每个进程的swap内存
#### f. 操作系统：内存大页
Linux 内核从 2.6.38 开始支持内存大页机制, 支持 2MB 大小的内存页分配，而常规的内存页分配是按 4KB 的粒度来执行的。
- redis在AOF重写/RDB时候fork是写时复制, 复制2MB太慢了.
- 关闭内存大页

#### g. CheckList.
	获取 Redis 实例在当前环境下的基线性能。
	是否用了慢查询命令？如果是的话，就使用其他命令替代慢查询命令，或者把聚合计算命令放在客户端做。
	是否对过期 key 设置了相同的过期时间？对于批量删除的 key，可以在每个 key 的过期时间上加一个随机数，避免同时删除。
	是否存在 bigkey？ 对于 bigkey 的删除操作，如果你的 Redis 是 4.0 及以上的版本，可以直接利用异步线程机制减少主线程阻塞；如果是 Redis 4.0 以前的版本，可以使用 SCAN 命令迭代删除；对于 bigkey 的集合查询和聚合操作，可以使用 SCAN 命令在客户端完成。
	Redis AOF 配置级别是什么？业务层面是否的确需要这一可靠性级别？如果我们需要高性能，同时也允许数据丢失，可以将配置项 no-appendfsync-on-rewrite 设置为 yes，避免 AOF 重写和 fsync 竞争磁盘 IO 资源，导致 Redis 延迟增加。当然， 如果既需要高性能又需要高可靠性，最好使用高速固态盘作为 AOF 日志的写入盘。
	Redis 实例的内存使用是否过大？发生 swap 了吗？如果是的话，就增加机器内存，或者是使用 Redis 集群，分摊单机 Redis 的键值对数量和内存压力。同时，要避免出现 Redis 和其他内存需求大的应用共享机器的情况。
	在 Redis 实例的运行环境中，是否启用了透明大页机制？如果是的话，直接关闭内存大页机制就行了。
	是否运行了 Redis 主从集群？如果是的话，把主库实例的数据量大小控制在 2~4GB，以免主从复制时，从库因加载大的 RDB 文件而阻塞。
	是否使用了多核 CPU 或 NUMA 架构的机器运行 Redis 实例？使用多核 CPU 时，可以给 Redis 实例绑定物理核；使用 NUMA 架构时，注意把 Redis 实例和网络中断处理程序运行在同一个 CPU Socket 上。




## 11.10

### 20 | 删除数据后，为什么内存占用率还是很高？

当数据删除后, Redis释放的内存空间会由内存分配器管理, 并不会立即返回给操作系统. 操作系统仍然会记录着 Redis 分配了大量内存
风险点：Redis 释放的内存空间可能并不是连续的, 这些不连续的内存空间很有可能处于一种闲置的状态.

#### a. 什么是内存碎片？
![例子](https://static001.geekbang.org/resource/image/23/df/23ebc99ff968f2c7edd0f8ddf7def8df.jpg)
分散的空间就是碎片.

#### b. 内存碎片产生内因: 内存分配器的分配策略
内存分配器一般是按固定大小来分配内存，而不是完全按照应用程序申请的内存空间大小给程序分配
jemalloc按照2^n字节分配.

#### c. 外因：键值对大小不一样和删改操作
![](https://static001.geekbang.org/resource/image/46/a5/46d93f2ef50a7f6f91812d0c21ebd6a5.jpg)


#### d. INFO 命令查看内存碎片
mem_fragmentation_ratio 内存碎片比例, =used_memory_rss/ used_memory, 分配的空间和使用的比率.
如果比率大于1.5, 说明使用率低于67%.

#### e. 如何清理内存碎片
1. 重启Redis
2. `config set activedefrag yes` 配置redis自动内存碎片清理, 主线程操作.
	active-defrag-ignore-bytes
	active-defrag-threshold-lower 两个必要限制条件打倒后就会清理
	还有2个参数限制内存清理的CPU使用.

问题: 如果mem_fragmentation_ratio<1, 说明Redis的物理内存要小于总内存, 一部分数据在Swap中.



## 11.11: 10日看了30%的21.

### 21 | 缓冲区：一个可能引发“惨案”的地方
主从同步的时候需要用缓冲区, 接收client命令的时候需要Buffer

#### a. 客户端输入和输出缓冲区
输入缓冲区把client的请求命令存期来, 输出buffer把数据缓存起来.
![输入输出buffer](https://static001.geekbang.org/resource/image/b8/e4/b86be61e91bd7ca207989c220991fce4.jpg)
buffer溢出场景:
1. big key
2. 处理阻塞, 请求buffer溢出.

#### b. 如何应对输入缓冲区溢出？
- 使用`CLIENT LIST`命令查看buffer使用情况
	qbuf, qbuf-free 使用和未用的queryBuffer
	当多个客户端连接占用的内存总量，超过了 Redis 的 maxmemory 配置项时（例如 4GB），就会触发 Redis 进行数据淘汰
- 解决角度: 大buffer, 加快数据命令的发送和处理速度
- 大buffer: redis中代碼设定输入buffer1GB, 不能设置.

#### c. 如何应对输出缓冲区溢出？
Redis的响应: 一个大小为 16KB 的固定缓冲空间, 用来暂存 OK 响应和出错信息; 另一部分动态增加的缓冲空间, 用来暂存大小可变的响应结果.
- output buffer溢出场景: 
	响应bigkey; 
	执行MONITOR命令: 会持续相应Redis的监控, 不用再生产; 
	缓冲区大小设置得不合理: `client-output-buffer-limit` 设置buffer的上限, 持续写入数据的数量和时间上限.
		缓冲区有3种: 用户client和订阅channel的subClient, 主从同步buffer
		normal参数配置普通客户端. 通常对buffer上线, 持续写入量和持续写入时间不限制
		pubsub参数, 可以设置8M的缓冲大小, 60s 2m的流量限制: `client-output-buffer-limit pubsub 8mb 2mb 60`
		slave参数, 可以设置全量复制buffer的大小.
		
#### d. 主从集群Buffer
全量复制和增量复制两个buffer: 全量复制每个slave一个, 增量复制是一个环形链表. 不同于TCP的读写缓冲区.
1. replication buffer: 全量复制
	![](https://static001.geekbang.org/resource/image/a3/7a/a39cd9a9f62c547e2069e6977239de7a.jpg)
	master为每个slave准备一个复制缓冲区(本质是client的outputBuffer), 在RDB文件传输过程的写命令放进去, RDB传输完成后, slave接收复制缓冲区的数据.
	如果RDB传输慢, 那么复制缓冲区容易溢出. 溢出后关闭连接, 复制失败.
	- 如何避免: 控制RDB的大小, 尽量使用Cluster. 配置`client-output-buffer-limit`,
		master上复制缓冲区的内存开销是每个从节点客户端输出缓冲区占用内存的总和. 大主从Redis会有压力.
2. repl_backlog_buffer: 增量复制
	![](https://static001.geekbang.org/resource/image/ae/8f/aedc9b41b31860e283c5d140bdb3318f.jpg)
	master把写命令同步给slave, 同时把命令写入复制挤压缓冲区, 一旦节点失联, 重连后就从复制挤压缓冲区拿到未接收的数据.
	问题:??????????????写命令同步给slave, 是用的复制缓冲区么?还是直接发给slave的? 用的是长连接 直接发送的, 放在TCP缓冲区, 内核负责TCP发送.
	

### 期中测试题 | 一套习题，测出你的掌握程度
选择题60分
1. Redis 在接收多个网络客户端发送的请求操作时，如果有一个客户端和 Redis 的网络连接断开了，Redis 会一直等待该客户端恢复连接吗？为什么？



## 11.12 0, 11.16

### 23 | 旁路缓存：Redis是如何工作的？

#### a. 缓存的特征
系统里不同层之间的访问速度不同, 所以需要缓存加速.
1. 缓存是一个快速子系统
- CPU 里面的末级缓存LLC, 用来缓存内存中的数据, 避免每次从内存中存取数据;
- 内存中的高速页缓存 page cache, 用来缓存磁盘中的数据, 避免每次从磁盘中存取数据.
2. 缓存的容量小于慢速系统, 不能缓存所有的.

#### b. Redis 缓存处理请求的两种情况
- 缓存命中: 直接读取redis
- 缓存缺失: 需要从数据库中把数据写入redis, 更新缓存.
![缓存处理请求的两种情况](https://static001.geekbang.org/resource/image/6b/3d/6b0b489ec0c1c5049c8df84d77fa243d.jpg)

#### c. Redis 作为旁路缓存
旁路缓存: redis是一个独立的数据库系统, 在application中做各种CRUD操作. 

#### d. 缓存类型: 
1. 只读缓存: 最新数据在DB中
![](https://static001.geekbang.org/resource/image/46/cd/464ea24a098c87b9d292cf61a2b2fecd.jpg)
应用: 修改不多的查看缓存.
2. 读写缓存: 最新数据在redis中
更新的时候, 都在redis中, 同步/异步写回.
![](https://static001.geekbang.org/resource/image/00/66/009d055bb91d42c28b9316c649f87f66.jpg)
应用: 抢购扣库存.
3. 两种缓存的区别:
只读缓存: 读不到就算了, 确保DB中数据为准, 修改DB然后干掉redis.优先保证了数据一致性.
读写缓存: 速度优先. 但会出现缓存和数据库共同修改的数据不一致.
消息队列同步可做到最终一致性.


### 24 | 替换策略：缓存满了怎么办: 缓存数据的淘汰机制

#### a. 缓存大小设置:
20%的数据不一定能贡献80%的访问量, 不能简单地按照"总数据量的20%"设置缓存最大空间容量. 应该在0.15-0.3之间动态调整.
`CONFIG SET maxmemory 4gb` 设置redis大小.

#### b. redis缓存淘汰策略
4.0由6种增加到8中:
![8种淘汰策略分类](https://static001.geekbang.org/resource/image/04/f6/04bdd13b760016ec3b30f4b02e133df6.jpg)
1. 优先使用`allkeys-lru`策略: 利用LRU剔除冷数据
LFU是基于访问频次的模式，而LRU是基于访问时间的模式。

#### c. 如何处理被淘汰的数据？
redis没有处理, 直接删, 需要我们再修改数据时候把脏数据刷回DB, 也就是读写缓存至少要做到.



### 25 | 缓存异常（上）：如何解决缓存和数据库的数据不一致问题？

#### a. 缓存和数据库的数据不一致是如何发生的？
redis是旁路缓存, DB和redis的修改不能原子化.

#### b. 如何解决数据不一致问题？
1. 重试机制: 利用消息队列, 没有成功update的丢进消息队列里.
情况一：先删除缓存，再更新数据库: ![](https://static001.geekbang.org/resource/image/85/12/857c2b5449d9a04de6fe93yy1e355c12.jpg)
解决: 更新完数据库后, sleep一会再删一次缓存. *延迟双删* 确保DB中数据为准
情况二：先更新数据库值，再删除缓存值: ![](https://static001.geekbang.org/resource/image/a1/0b/a1c66ee114yyc9f37f2a35f21b46010b.jpg)
问题较小, 只是会造成一小段时间内的数据不一致. 

2. 总结: 
数据库/redis fail的问题, 采用重试.
redis和db之间的并发问题: 采用延迟双删 或者 先DB再redis.
![问题归总](https://static001.geekbang.org/resource/image/11/6f/11ae5e620c63de76448bc658fe6a496f.jpg)

3. 问题: redis修改的时候, 不是删除而是修改会有什么问题: 会把只读缓存变成了读写缓存, redis和DB的数据不一致问题凸显, 可能会丢数据. 而只读不会丢DB的新数据.

#### c. 抢购问题不能修改DB的情况呢??????????


### 26 | 缓存异常（下）：如何解决缓存雪崩、击穿、穿透难题？

#### a. 缓存雪崩
雪崩是缓存不能被请求命中了: 
![](https://static001.geekbang.org/resource/image/74/2e/74bb1aa4b2213e3ff29e2ee701e8f72e.jpg)
![服务降级](https://static001.geekbang.org/resource/image/4a/a8/4ab3be5ba24cf172879e6b2cff649ca8.jpg)
1 大量数据同时过期:
	- 失效时间不要相同
	- 服务降级: 雪崩时候非核心数据可以不要查库, 返回Null, 只放过核心数据.
![服务熔断](https://static001.geekbang.org/resource/image/17/b5/17d39f6233c3332161c588b42eccaeb5.jpg)
![请求限流](https://static001.geekbang.org/resource/image/d5/54/d5a0928e1d97cae2f4a4fb5b93e5c854.jpg)
2. redis 宕机:
	- 服务熔断: 暂停对缓存接口的访问, 直接是null.
	- 限流
	- 构建高可用redis cluster

#### b. 缓存击穿
具体的某个热点数据, 缓存拦不住请求, 都打到DB了.
![缓存击穿](https://static001.geekbang.org/resource/image/d4/4b/d4c77da4yy7d6e34aca460642923ab4b.jpg)
- 热点数据不设置过期时间


#### c. 缓存穿透: 
redis&DB都没有数据, 每次都要两个都访问一遍.
![缓存穿透](https://static001.geekbang.org/resource/image/46/2e/46c49dd155665579c5204a66da8ffc2e.jpg)
1. 误删除DB数据:
2. 恶意攻击:
3. 解决方法: 
	- 缓存缺省值, 拦截住下次request
	- 布隆过滤器快速判断数据是否存在, 校验后再查DB: 写入DB/查为null时候标记, 下次就用来判断.
	- 前端接口请求校验.
	
总结: ![总结](https://static001.geekbang.org/resource/image/b5/e1/b5bd931239be18bef24b2ef36c70e9e1.jpg)
尽量预防: 不设置相同过期时间, 热点数据不要设置过期时间, 增强请求校验, 缓存缺省值.


问题: 应对雪崩的限流熔断可以用在穿透上么? 不可以, 雪崩是DB压力大, 但DB有数据, 穿透即使访问DB也没数据, 要拦住请求.



### 27 | 缓存被污染了，该怎么办？
无用的数据进入缓存, 就会污染缓存.

#### a. 如何解决缓存污染问题？
1. volatile-random 和 allkeys-random: 随机删除, 没用.
2. volatile-ttl: 剩余存活时间长短不能完全代表缓存数据能不能接着用, 不太好.
3. LRU: 只看访问时间, 所以扫描时的查询时候, 无效.
4. LFU: least-frequency-use: 最小使用频率的算法: 根据访问次数链表, 访问次数相同根据LRU时间.

- **LRU实现策略**: 两个近似方法
	1. redisObj中由LRU字段记录时间戳
	2. 不维护全局链表, 随机采样, 根据LRU字段进行筛选, 统计意义上的相似.
	
- **LFU实现策略**: 24bite的lru字段拆分成两个部分
	使用LFU策略时候, 根据lru字段的后八位比较次数, 然后相同再比较时间.
	- 前16bit: 接着存储ldt接着存储访问时间戳
	- 后8bit: counter, 计数访问次数, 最多256
	- counter+1机制, 在lfu_log_factor运用算法过后才会加一
	- counter衰减机制: 每次访问根据lfu_decay_time控制衰减1的时间. 每分钟衰减value的.
	
#### b. 最合适的策略:
使用volatile-lfu干掉过期时间的key.
	
问题: 使用了 LFU 策略后, 缓存还会被污染吗? 
被污染的概率取决于LFU的配置，也就是lfu-log-factor和lfu-decay-time参数, 如果短期增加很快, 衰减很慢.




## ==============11.18

### 28 | Pika: 如何基于SSD实现大容量Redis？

redis容纳更多数据, 要么cluster-增加集群运维复杂度, 要么用大内存-增加DR压力. 
还可以通过SSD来实现大容量redis实例.

#### a. 大内存 Redis 实例的潜在问题
1. 快照RDB的生成和恢复.
2. 主从同步, 主从切换的耗时.

#### b. Pika 的整体架构
![pika架构](https://static001.geekbang.org/resource/image/a1/e7/a1421b8dbca6bb1ee9b6c1be7a929ae7.jpg)

#### c. Pika 使用RocksDB 基于 SSD 保存更多数据

#### d. Pika 如何实现 Redis 数据类型兼容？




### 29 | 无锁的原子操作：Redis如何应对并发访问？
加锁, 原子操作;

#### a. 并发访问中需要对什么进行控制？
对修改进行控制. 
虽然加锁保证了互斥性，但是加锁也会导致系统并发性能降低。

#### b. Redis 的两种原子操作方法
1. redis编码实现单命令操作: incr/decr.
2. **Lua脚本原子执行**: Lua脚本尽量简单核心操作
 


### 30 | 如何使用Redis实现分布式锁？

#### a. 单机上的锁和分布式锁的联系与区别
- 单机上的锁是一个信号量, 标记好就表示获得锁.
- 分布式锁的锁变量需要由一个共享存储系统来维护.
	要考虑 加锁解锁的原子性, DR锁的可靠性.
	
#### b. redis单节点分布式锁
设置 唯一ID 有过期时间的 锁变量

#### c. 基于多个 Redis 节点实现高可靠的分布式锁
Redlock: 
1. client依次向多个独立redis实例单独加锁
2. 如果半数以上实例枷锁成功, 分布式锁拿到.
3. 计算加锁耗费的时间, 如果超过了锁有效期其实是无效锁, 剩余时间就是有效期-加锁耗时.
- 如果master down掉, 此时slave没有同步的话, 其他client有可能拿到锁.






## =========== 11.19

### 31 | 事务机制：Redis能实现ACID属性吗？

#### a. Redis 如何实现事务？
MULTI开启事务, EXEC执行事务, discard是主动放弃事务.
如果事务执行出错, 还会继续执行所有其他的命令.

原子性: 事务执行出错接着执行其他的命令, 不具有原子性. 
一致性: slave-master同步, 可以保证最终一致的.
隔离性: 可以保证lua脚本里面的内容, 输入exec之后的隔离性. 之间的可以用watch然后自己去处理.
持久性: 不能, 即使AOF的always, 写后日志没有同步给slave宕机也没有了.


问题: 执行事务时，如果 Redis 实例发生故障，而 Redis 使用了 RDB 机制，那么，事务的原子性还能得到保证吗:
能, 因为RDB也是主线程在做, 主线程做lua的时候顾不上RDB

建议: pipeline结合事务使用, 可以保证隔离性.



### 32 | Redis主从同步与故障切换，有哪些坑？

问题: 主从数据不一致、读到过期数据，以及配置项设置得不合理从而导致服务挂掉

#### a. 主从数据不一致
主写从读 会有不一致. 
- 网络硬件条件要好
- 监控主从复制进度:master_repl_offset, slave_repl_offset.


#### b. 读到过期数据
redis惰性删除/定期删除不能保证把所有的过期数据都马上干掉, slave只接受同步命令不会删数据.
3.2版本的slave开始对过期数据返回空.
- 主从同步有延迟, 对于EXPIRE 和 PEXPIRE命令, 执行命令开始计算存活时间, 会使得slave的数据死的晚.
- **使用EXPIREAT 和 PEXPIREAT设置具体时间点.**


#### c. 不合理配置项导致的服务挂掉

1. protected-mode: 限制redis能不能被localhost之外的ip访问.
	yes的时候 redis的哨兵模式没有办法交流了.

2. cluster-node-timeout: cluster中实例心跳响应超时时间. 调大点, 半数以上的实例正常cluster才正常. 在半数以上的主从切换就可能会挂.

![汇总](https://static001.geekbang.org/resource/image/9f/93/9fb7a033987c7b5edc661f4de58ef093.jpg)






## 11/20

### 33 | 主从切换脑裂：一次奇怪的数据丢失

#### a. 为什么会发生脑裂？
1. 判断脑裂:
在排查客户端的操作日志时, 在主从切换后有客户端仍在和原主库通信, 并没有到新主库. 
2. 判断脑裂过程, 为什么丢失数据:
在切换过程中, 客户端仍与原主库通信, 说明原主库没有真的发生故障. 猜测主库没有响应哨兵的心跳, 被哨兵错误地判断为客观下线的.
原因: 数据采集导致redis CPU利用率突高.
脑裂产生问题的本质原因是，Redis 主从集群内部没有通过共识算法，来维护多个节点数据的强一致性。它不像 Zookeeper 那样，每次写请求必须大多数节点写成功后才认为成功

#### b. 为什么脑裂会导致数据丢失？
脑裂的时候 sentinel会向所有他认为的slave发送slaveof命令, 然后原master会接受新主库的RDB, 清空自己的数据, 造成了client在旧主写入的都丢了.

#### c. 如何应对脑裂问题？
因为redis响应慢client没有放弃它, 导致了缓过来还写. 
可以配置: min-slaves-to-write, min-slaves-max-lag 确保client写入的保证slave及时接收, 如果master处理不了就会被限制client访问, 也就不接受client的请求了.
流程说明: 假设我们将 min-slaves-to-write 设置为 1，把 min-slaves-max-lag 设置为 12s，把哨兵的 down-after-milliseconds 设置为 10s，主库因为某些原因卡住了 15s，导致哨兵判断主库客观下线，开始进行主从切换。同时，因为原主库卡住了 15s，没有一个从库能和原主库在 12s 内进行数据复制，原主库也无法接收客户端请求了

https://online.visual-paradigm.com/w/tfrzbozw/app/diagrams/#diagram:workspace=tfrzbozw&proj=0&id=4





## 11/26

### 35 | Codis VS Redis Cluster：我该选择哪一个集群方案？
对于一个分布式系统来说，它的可靠性和系统中的组件个数有关, 和 Redis Cluster 只包含 Redis 实例不一样，Codis 集群包含的组件有 4 类.

#### a. Codis 的整体架构和基本流程
- codis server： Redis 实例, 增加了额外的数据结构, 支持数据迁移操作
- codis proxy： 接收转发请求到 codis server。
- Zookeeper 集群： 保存集群元数据, 例如数据位置信息和 codis proxy 信息.
- codis dashboard 和 codis fe: 共同组成了集群管理工具. dashboard 负责执行集群管理工作, 增删 codis server,codis proxy 和进行数据迁移. fe 负责提供 dashboard 的 Web.
![Codis 的整体架构](https://static001.geekbang.org/resource/image/c7/a5/c726e3c5477558fa1dba13c6ae8a77a5.jpg)

#### b. 关键问题: 数据如何在集群里分布？
也是通过slot映射完成codis-server和数据的存放的
1. 一共1024个slot [0,1, ..., 1023]. 每个server 上都有. 
2. 对key使用CRC32计算hash值. 客户端计算.
![](https://static001.geekbang.org/resource/image/d1/b1/d1a53f8b23d410f320ef145fd47c97b1.jpg)

#### c. 集群扩容和数据迁移如何进行?
启动新的 codis server 将它加入集群, 然后把部分slot数据迁移给它.
slot迁移是单个key, source server设置成只读数据然后开始, 有同步模式和异步非阻塞的. 
![slot迁移过程](https://static001.geekbang.org/resource/image/e0/6b/e01c7806b51b196097c393a079436d6b.jpg)
`SLOTSMGRTTAGSLOT-ASYNC numkeys`设置每次迁移的key数量.

增加 proxy 比较容易，我们直接启动 proxy，再通过 codis dashboard 把 proxy 加入集群就行。
![增加proxy](https://static001.geekbang.org/resource/image/70/23/707767936a6fb2d7686c84d81c048423.jpg)

#### d. 通讯协议:  RESP
使用 Redis 单实例时，客户端只要符合 RESP 协议，就可以和实例进行交互和读写数据。

#### e. 怎么保证集群可靠性？
codis-server可以用主从.
codis proxy 使用 Zookeeper 集群保存路由表.
codis dashboard 和 codis fe 来说，它们主要提供配置管理和管理员手工操作
![](https://static001.geekbang.org/resource/image/02/4a/0282beb10f5c42c1f12c89afbe03af4a.jpg)

#### f. 切片集群方案选择建议
![codis-vs-cluster](https://static001.geekbang.org/resource/image/8f/b8/8fec8c2f76e32647d055ae6ed8cfbab8.jpg)
*从数据迁移性能维度来看，Codis 能支持异步迁移*

问题: 假设 Codis 集群中保存的 80% 的键值对都是Hash,每个Hash集合的元素10万~20万个, 单个元素2KB. 迁移会对Codis的性能造成影响吗?
答: 异步情况下不会吧, 每个只有2k, source发送出去就是放key.


















