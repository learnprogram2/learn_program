# Concepts

## Overview
上面的手动训练解释了Flink的状态流的基本概念, 也提供了怎么使用这些机制. 状态流处理在上面的`DataPiplines&ETL`里面介绍过概念, 也在`FaultTolerance`里面进一步使用了. 实时流也在`StreamingAnalytics`里面介绍了.

Concepts这一节, 介绍更深的理解Flink架构怎么运行的

### Flink's APIs
Flink提供了多层的抽象结构为我们的开发: ![](https://ci.apache.org/projects/flink/flink-docs-release-1.11/fig/levels_of_abstraction.svg)

1. 最底层提供了 `Stateful and Timely Stream processing`, 状态流是通过ProcessFunction内置到DataStreamAPI里面的. 这让我们可以自由的处理n个流的事件, 并且提供了持久化, 容错的state. 除此之外, 用户可以注册eventTime和processingTime的callback, 允许我们可以实现更复杂的计算.
2. 实际上, 很多应用不需要使用底层api, 可以使用CoreAPI进行编程就好了.DataStream/DataSetAPIs.这些流API提供了通用的data处理模块, 比如多种形式的用户自定义转换: Joins, aggregations, windows, state... API里面计算用的DataType是我们编程语言里面的class.
	底层的ProcessFunction和DataStreamAPI世纪城的, 可以根据需要使用底层的抽象(ProcessFunction). DataSetAPI提供了其他的有界数据的原语: 迭代/循环...
3. TableAPI是声明表的DSL. 可以动态的修改表(表示stream的时候), tableAPI遵循可扩展的关系模型: 有schema(有点像DB), 提供其他的对比操作(select, project, join, group-by..) tableAPI不提供具体操作代码, 表达能力比CorAPI差, 但是更简单.
	我们可以在table和dataStream/dataSet之间无缝转换, 混合使用.
4. 最高层的抽象是SQL, 有点像tableAPI, 但是直接使用SQL查询. SQL抽象与TableAPI紧密结合, 可以在tableAPI上面的表里查询.


## Stateful Stream Processing
### State是什么
stream里面的操作有的是每个event独立的, 有的需要记住跨event的信息(比如window), 这些操作叫做有状态的.
状态操作举例:
1. app在寻找具体event模式的时候, state可以存储event出现的顺序. 
2. 在整合一段时间的数据时候, state应该拿着在pending的汇总.
3. 在训练机器学习模型的时候, state应该拿着当前的模型参数.
Flink需要了解state, 使用checkpoint和savepoint做容错处理. state也允许rescal, 说明flink管理state跨并行度之间的重分配. 
`Queryable state`允许我们可以在flink外面拿到state.
在使用state的时候, 也可以阅读`Flink state backends`, flink 提供了不同的statebackend来指定怎么存储. 

### Keyed State















## Flink 文档 Concepts

> 现在是1.10版本的, 后面完成之后改成1.11版本的.

分布式流处理的平台, 为数据流上的分布式计算提供数据分发, 通信和容错.

### 一. 数据流编程模型

#### 1. 抽象层次

![编程抽象级别](Flink-Doc-1.assets/levels_of_abstraction.svg)

#### 2. 程序和数据流

Flink程序的基本构建块是**流**和**转换**, 由**流**和转换 **算子组成**. 

**流**是数据流, 类似于任意有**向无环图** _（DAG）_

**转换**是将一个或多个流作算子操作, 并产生一个或多个输出流.

#### 3. 并行数据流

Flink程序本质上是并行和分布式的,

*流*具有一个或多个**流分区**, 可以一对一/hash分发模式在多个算子间传递数据.

每个 _算子_具有一个或多个 **算子子任务**: 算子子任务彼此独立，并且可以在不同的线程中执行

#### 4. 窗口

聚合事件(计数, 总和) 在流上由**窗口**限定, 窗口可以是时间/数量驱动的.

#### 5. 时间

事件时间, 摄取时间(source算子输入flink的时间), 处理时间(算子计算的本地时间).

#### 6. 有状态的算子操作

某些 算子操作会记住多个事件的信息, 这种算子操作是有状态的.

状态 算子操作的**状态保持在键/值存储的状态中**, **状态被分区**并严格地与有状态算子读取的流一起分发.

![状态和分区](Flink-Doc-1.assets/state_partitioning.svg)

#### 7. 容错checkpoint

Flink使用**流重放**和**CheckPoint**的组合实现容错,  恢复算子的状态(checkpoint),并从checkpoint重放事件可以恢复流数据.

#### 8. 流处理批处理

Flink将批处理作为流处理, 只不过这个流是有终点的.

只不过, 批处理: 容错不使用checkpoint(而用完全重放), dataset API有状态算子操作使用简化的内存/核外数据结构, 不是键值索引.



### 二. Distributed Runtime (分布式运行时环境)

#### 1. task 和 operatorChain

分布式执行, 每个任务由一个线程执行. Reduce线程到线程切换和缓冲的开, 降低延迟的同时提高整体吞吐量.

#### 2. TaskManager, JobManager, Client

Flink运行时包括两种类型的进程, Task Manager, 和 Job Manager 部署可以在独立集群里, 也可以在YARN资源框架里.

1. JobManager(Master): 协调分布式致性, 安排任务, checkpoint, 协调故障恢复.

   至少有一个JobManager

2. TaskManager: 执行子任务的工人.

3. Client用于提交数据/任务的

   ![执行Flink数据流所涉及的过程](Flink-Doc-1.assets/processes.svg)

#### 3. Slot 和 资源

每个worker都是一个JVM进程, 可以在JVM里放n个子任务, 子任务需要资源, worker 有slot, slot代表资源子集, 一个slot可以放一个子任务. 例如, 具有三个插槽的TaskManager将其1/3的托管内存专用于每个插槽, 不会发生CPU隔离; 当前插槽只分离任务的托管内存.

一个operatorChain里的operator可以共享slot

#### 4. 状态后台

1. 存储键/值索引的确切数据结构取决于所选的[状态后台](https://flink.sojb.cn/ops/state/state_backends.html), 可以将数据存储在内存中的哈希映射中, 也可以使用[RocksDB](http://rocksdb.org/)作为键/值存储.

2. 获取键/值状态的时间点SNAPSHOT, 将snapshot存储为checkpoint的一部分.

   ![检查点和SNAPSHOT](Flink-Doc-1.assets/checkpoints.svg)

#### 5. Savepoint

savepoint相当于redis的RDB, 手动触发的checkpoint, 获取程序的SNAPSHOT并将其写入状态后台

savepoint与定期checkpoint类似, 不同之处在于savepoint**由用户触发**, 并且在较新的检查点完成时**不会自动过期**. 









