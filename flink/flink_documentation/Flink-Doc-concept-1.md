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
keyed-state可以看做成内置的k-v存储, state被分区和分布紧随着stream, 可以被statefulOperator读到. 此外, k-v state只能在keyedStream里面. 对齐key和state可以保证state的更新都是本地操作, 确保了没有事务开销的一致性. key的对齐也允许Flink把state分发, 透明的调整stream的分区. 
![keyedStateConceple](https://ci.apache.org/projects/flink/flink-docs-release-1.11/fig/state_partitioning.svg)
keyed-state进一步被组织成keyGroup, keyGroup是原子单位, Flink可以使用它重新分发state.keyGroup的数量就是并行度的数量, 在运行时候, 每一个keyedOperator的并行度示例都可以使用一个或者多个keyGroup.

### State持久化
Flink通过`stream replay`和`checkpointing`的结合来实现容错处理. checkpoint会在每一个inputStream标记一个特殊点和对应的operator的对应state. stream流可以从checkpoint里面恢复, checkpoint维持着一致性, 通过恢复operator的state并且从checkpoint开始replay数据(exactly-once语义). 
checkpoint的间隔是过分容错和恢复时间的平衡. 
容错机制不断地做dataflow的snapshot, 如果是state比较小, 那么非常快. state被存在配置的地方(一般是nfs之类的分布文件系统).

遇到了程序出错的时候, flink把流停住. 系统然后重启operators, 把他们设置到最新的checkpoint状态. inputStream也恢复到state快照的状态, 所有的record在重启之后的dataflow运行, 并且保证不会影响之前的checkpoint state.
> checkpoint默认关闭. 为了保证这种guarantee, dataStream的source必须保证可以把流恢复到之前的一个点上. 
> 因为flink的checkpoint是通过分布式的snapshot实现的, 我们可以混用这两个词. 一般来说snapshot代表checkpoint或者savepoint.

#### 1. Checkpoint
flink的容错机制最重要的就是画分布的stream和operatorState的一致性快照. 这些快照在failure的时候就当成一致性的checkpoint. 制作这些snapshot的机制叫做`[Lightweight Asynchronous Snapshots for Distributed Dataflows](http://arxiv.org/abs/1506.08603)`. 灵感来自于分布式快照的标准的Chandy-Lamport算法, 并且特殊的定制了.
要记住, checkpoint所有相关的操作都可以异步, checkpoint屏障没有锁的步骤, operator也可以异步的快照state.
flink1.11里面checkpoint可以不需要对齐做snapshot. 之前需要

##### 1.1 Barriers(屏障)
flink分布快照最重要的一个element就是流的barrier. 这些屏障在流的里面, 随着record流动, 是六的一部分. barrier不会跳过record, 它们严格的排队, 把record分割成一个一个的set. 每个barrier带着snapshot的ID, 前面的record就都到snapshot进去了.  barrier不会打扰flow 所以非常的轻便. 多个barrier代表着不同的snapshot可以同一时间在流里面. 不同的snapshot也可以并发的进行. 
![checkpoint和它的barrier](https://ci.apache.org/projects/flink/flink-docs-release-1.11/fig/stream_barriers.svg)
stream的屏障在每一个并行度的流里. 每一个并行流里面的屏障前面最后一条会报告给checkpoint的coordinator(就是jobManager)
屏障会接着跟着流流动, 中间的operator接收到了所有inputStream的屏障, 就发给下游一个屏障, 最后sink收到了所有的屏障, 就会告诉checkpointCoordinator自己的snapshot完成了, 所有的sink都完成了之后就结束了. 
一旦snapshot_n完成了, job不会再向stream要Sn之前的数据了, 因为都已经完整的消费过了.
![snapshot](https://ci.apache.org/projects/flink/flink-docs-release-1.11/fig/stream_aligning.svg)
接收多个inputStream的时候, 需要对齐barrier. 上面的图也介绍了. 
- 在接入的六里面一拿到barrier_n, 就不接收这个流里的数据了, 等把其他的流里面的barrier都接受到. 不然的话怕混了.
- 收到了所有的barrier_n, 就开始发送缓存的record, 接着再把这个barrier_n提交出去.
- 开始做state的快照, 然后接着从inputStream里面接收数据(先拿缓存里面的) 
- operator会把state异步的写道stateBackend里面
所有接收多个流的operator都需要对齐, 或者接受流的shuffle之后的数据也需要. 

##### 1.2 快照 operator state
在operator包含有任何形式的state的时候, 这些state必须做snapshot.
在收到了所有的snapshot_barrier的时候就可以开始快照了, 在提交这些barrier之前完成. 这个时间带你上, 所有的更新都做了, 还没有之后record的影响. 因为state可能很大, 存在配置的_statebackend_里. 默认的存放在jobmanager的内存里, 但在生产上要用分布式文件系统可靠一点. state存好了之后, operator知道了checkpoint, 然后把barrier提交给下游, 自己接着处理.
snapshot包含:
	- 每一个source并行度的offset/position(snapshot开始的时候)
	- 每一个operator的state存放的地址.
	![checkpoint_process](https://ci.apache.org/projects/flink/flink-docs-release-1.11/fig/checkpointing.svg)
	
##### 1.3 恢复 recovery
恢复机制比较直接, 遇到了錯誤, flink选择最新完成的checkpoint. 系统会重放整个dataflow, 然后给每个operator恢复state. source设置到当时的position. 
如果state是增长式的snapshot的时候, operator从上一次最近的fullsnapshot开始, 然后把之后的snapshot修改放到这个state上面.
可以看[RestartStrategies](https://ci.apache.org/projects/flink/flink-docs-release-1.11/dev/task_failure_recovery.html#restart-strategies)

#### 2. 不对齐的checkpointing
Flink1.11开始, checkpoint也可以不对齐了. 基本思想是 checkpoint通过包含了in-flight的data, 来克服这些数据带来的扰乱. 会把这个barrier里面的record和state一起快照.
注意: 这个方法更接近`Chandy-Lamport`算法, 但是Flink还会insert barrier在source, 来避免checkpointCoordinator的放太多~
![how handle the unaligned checkpoint](https://ci.apache.org/projects/flink/flink-docs-release-1.11/fig/stream_unaligning.svg)
上面, unaligned的checkpoint分三步:
	- operator堆缓存里面的第一个barrier做checkpoint
	- 快速的把这个barrier放在输出buffer的最后, 为了发送做准备. 
	- 把所有的barrier之前应该处理的数据做一步存储, 然后创建快照. 
不对齐的checkpoint确保了barrier尽快的到达sink. 最合适那种有一个比较慢的流动的应用, 对齐时间可能长达几个小时. 但是它增加了I/O压力, 其他的限制可以看[ops](https://ci.apache.org/projects/flink/flink-docs-release-1.11/ops/state/checkpoints.html#unaligned-checkpoints)
注意: savepoint是必须对齐的.

###### 不对齐的recovery
operator收起恢复in-flight数据. 除此之外和对齐的recovery一样.

#### 3. state backends
k-v的额外数据存放在state backend里面. 一个state backend存在内存里的hashMap里, 另一个stateBackend使用RockesDB做kv的存储. 
除了定义state的structure之外, stateBackend也实现了存储checkpoint的state. 
![](https://ci.apache.org/projects/flink/flink-docs-release-1.11/fig/checkpoints.svg)

#### 4. Savepoints
所有使用checkpoints的程序都可以从savepoint里面恢复, savepoint允许我们不损失任何state的时候来升级程序.
savepoint是手动触发的checkpoint, 会堆整个程序做快照, 并且把它写到stateBackend. 依赖于定期的checkpoint.
savepoint类似于checkpoints, 只是这是手动触发的, 不会自动失效.

#### Exactly Once vs AtLeastOnce
对齐的步骤可能会给stream处理添加latency. 一般情况下这额外的latency是几毫秒, 但是有的时候增长很多. 对于要求很低latency的程序, flink有一个开关在stream checkpoint对齐的时候跳过.checkpoint快照在operator收到每个input的barrier的时候还会接着做. 
对齐跳过之后, operator接着处理input, 甚至在checkpoint_n到了之后有跳过了很多个barrier. 在恢复的是哦胡, record会重复的出现, 因为record可能在checkpoint_n里面处理过了, 然后还会在n之后重放. 
就是过去了, 然后不记录它为成功, restore的时候肯定又来了一遍. 
注意: 对齐旨在operator有多个前置操作(join)的时候或者有多个sender(比如repartition/shuffle)的时候进行. 所以, dataflow在流里面的parallel操作的时候实际只会exactlyOnce.

### State and Fault Tolerance in Batch Programs 批处理里面的state和容错.
flink把批处理当作特殊的流处理执行, 批处理是有界的. dataSet内部就是一个流. 上面的概念也试用于批处理. 只有几个例外:
1. 不会做checkpoint, recovery会replay stream. 
2. stateful操作在dataSetAPI里面指挥用简单的in-memory/out-ofcore 数据结构, 而不是key/value的indexes.
3. dataSet API引入了特殊的同步的遍历. [iterations](https://ci.apache.org/projects/flink/flink-docs-release-1.11/dev/batch/iterations.html)









































> =========================================================================================================================
> 下面是Flink1.10版本的, 感觉1.11版本的讲地更形象, 把checkpoint的原理画图非常容易理解.
> =========================================================================================================================

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









