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



## 11.12

### 23 | 旁路缓存：Redis是如何工作的？









































