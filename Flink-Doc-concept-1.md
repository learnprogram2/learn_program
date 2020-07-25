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









