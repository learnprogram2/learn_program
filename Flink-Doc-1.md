## Flink 文档

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



## Application Development



### 三. 开发: Configuring Dependencies, Connectors, Libraries

Flink开发的程序开发和运行偶需要依赖一些Flink libraries, 比如说Kafka... 

#### 1. Flink Core 和 Application Depenencies

主要是两类依赖/库

1. Flink Core Dependencies: Flink本身由运行系统所需的一组类和依赖项组成, 例如协调，网络，检查点，故障转移, API, 算子操作(如窗口), 资源管理等. Flink运行时核心.

   核心类和依赖放在`flink-disk`jar中, 核心依赖项不包含任何连接器或库

   ```xml
   ----------
     <groupId>org.apache.flink</groupId>
     <artifactId>flink-java</artifactId>
     <scope>provided</scope>
   -----------
   <artifactId>flink-streaming-java_2.11</artifactId>
   <scope>provided</scope>
   ```

   

2. User Application Dependencies: 包含所有的连接器, 格式, 自定义的功能

   ```xml
   <dependency>
       <groupId>org.apache.flink</groupId>
       <artifactId>flink-connector-kafka-0.10_2.11</artifactId>
       <version>1.7-SNAPSHOT</version>
   </dependency>
   之类的
   ```

3. 另外要注意hadoop依赖. 永远不必将Hadoop依赖项直接添加到您的应用程序, 需要具有包含Hadoop依赖关系的Flink设置

   是因为: hadoop交互发生在Flink的核心, 在用户应用程序启动之前就交互. hadoop的依赖树太大了, flink自己为了避免版本覆盖和依赖冲突.

   

### 四. 基本API概念:

Flink programs are regular programs that implement transformations on distributed collections(flink程序是常规的分布式集合转换的程序.)

集合从源创建, 最终通过接收器返回. 程序可以独立运行/嵌入其他程序, 可以在本地JVM运行, 也可以在ECS运行.

#### DataSet 和 DataStream

dataSet APi核心类在`org.apache.flink.api.java`包中

dataStream API核心类在`org.apache.flink.stream.api`包中

#### Flink 计划剖析

1. 获得一个`execution environment`，
2. 加载/创建初始数据, 拿到dataStream
3. 指定此数据的转换, process operator
4. 指定放置计算结果的位置, sink
5. 触发程序执行, execute

#### Lazy Evaluation: 

所有Flink程序都是懒惰地执行: 当执行程序的main方法时, 数据加载和转换不会直接发生. 而是创建每个 算子操作之后再致性.

懒评估可以使人构建复杂程序, 然后flink将其作为整体计划单元执行.

#### 指定Key

```java
DataSet<...> input = // [...]
DataSet<...> reduced = input
	// 在数据元集合上定义键,
  	.groupBy(/*define key here*/)
    // keys分组
  	.reduceGroup(/*do something*/);
DataStream<...> windowed = input
  .keyBy(/*define key here*/)
  .window(/*window specification*/);
```

#### 定义Tuples的键

....这些api用到再说吧











### 五. DataStream 

>  Flink DataStream API 流处理编程

#### 0. Overview

1. **DataSource**

   实现SourceFunction. 有很多来源:

   1. File-based
   2. Socket-based
   3. Collection-based
   4. Custom....

2. DataStream Transformation: 数据流的转换就看Operator

3. **DataSinks**

   自己去实现就好了

4. **迭代 Iterations**

   迭代流程序实现步进函数并将其嵌入到`IterativeStream`,  需要指定流的哪个部分反馈到迭代, 哪个部分使用`split`转换或转发到下游`filter`.

   ```java
   // 首先, 我们定义一个IterativeStream
   IterativeStream<Integer> iteration = input.iterate();
   // 业务代码, 循环内执行的逻辑
   DataStream<Integer> iterationBody = iteration.map(/* this is executed many times */);
   // 关闭迭代并定义迭代尾部, closeWith(feedbackStream)
   iteration.closeWith(iterationBody.filter(/* one part of the stream */));
   DataStream<Integer> output = iterationBody.filter(/* some other part of the stream */);
   ```

5. **执行参数 execution parameter**

   environment里面又ExecutionConfig, 可提供runtime的配置 ExecutionConfiguration可以在执行管理(ManagingExecution)文档里面

6. **容错**

   State & Checkpoints

7. **延迟控制(Controlling Latency)**

   因为数据单元会被缓存集体传输, 所以可能会有延迟问题.

   所以可以设置缓冲区来手动掌控: **env/operator**.setBufferTimeout(timeoutMillis)

8. **调试Debugging**

   Flink通过支持IDE内的本地调试

   1. 本地运行环境

   2. Collection DataSource: 

      Flink提供了特殊的数据源, 这些数据源由Java集合支持, 方便测试

   3. IteratorDataSInk(sink)

      ```java
      DataStream<Tuple2<String, Integer>> myResult = ...
      Iterator<Tuple2<String, Integer>> myOutput = DataStreamUtils.collect(myResult)
      ```

#### 1. Event Time

##### 1.0 Overview

1. **三个时间概念**

   1. Processing time(处理时间): 算子计算record的时间

   2. Event time(时间事件):事件发生的时间

   3. Ingestion time(摄取时间): 再source的时候![img](Flink-Doc-1.assets/times_clocks.svg)

   4. 设置时间特征

      数据流源的行为方式(例如, 它们是否将分配时间戳), 窗口 算子操作应该使用的时间概念

      ```java
      env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
      ```

      时间戳分配和watermark生成的指南在之后介绍

2. **EventTime 和 Watermark**

   Flink实现了Dataflow Model, event time 和 watermarks可以看下面的文章

   - [Streaming 101](https://www.oreilly.com/ideas/the-world-beyond-batch-streaming-101) by Tyler Akidau
   - The [Dataflow Model paper](https://research.google.com/pubs/archive/43864.pdf)

   **EventTime** : todo, 看不太懂现在.
   
   衡量事件时间进度的机制是**Watermark**, watermark是数据流的一部分, 带有时间戳*t.* 有点类似一个屏障, 意味着流里面, watermark的时间t之前的数据都存在了, 无序流里面用到, 在watermark到了之后operator可以把internalEventTimeClock调到watermark的时间.
   
   ![A data stream with events (Flink-Doc-1.assets/stream_watermark_in_order.svg) and watermarks](https://ci.apache.org/projects/flink/flink-docs-release-1.10/fig/stream_watermark_in_order.svg)
   
3. **Watermark In Parallel Stream**

   watermark在sourceFunction之内/之后生成, 每一个source的并行流都独立的生成watermark. 这些watermarks定义了eventTime, 在watermark流过operator的时候, 它更新了operator的eventTime.然后这个operator为下流的operator生成新的watermark, 消费多个流的operator的eventTime就是接受到最小的watermark的time

   ![Parallel data streams and operators with events and watermarks](Flink-Doc-1.assets/parallel_streams_watermarks.svg)

   注意, kafkaSource支持按照partition分watermark, 后文有提.

4. **Late Elements**

   迟到的元素也需要容错处理, 后面会提到.

5. **Idling Source(闲置的source)**

   如果只有数据来的时候生成watermark, 那么间歇时候window operator就不会被triggered.所以需要periodic watermark assigner.`SourceFunction.SourceContext#markAsTemporarilyIdle`, 后面的Allowed Lateness会讲

6. **Debugging Watermarks**

   后文Debugging篇会有

7. **How operators are processing watermarks**

   windowOperator收到watermark之后会评估触发那些window, 在处理完数据之后才会发送下游的watermark. 具体的实现在:`OneInputStreamOperator#processWatermark`， `TwoInputStreamOperator#processWatermark1`和`TwoInputStreamOperator#processWatermark2`

##### 1.1 Generating Timestamps / Watermarks

本小节是和run在EventTime的程序相关的. 设置run on EventTime需要在env上面设置

```java
final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime); // 设置eventTime
// TimeCharacteristic这个Eum里面的三个时间, Operations like windows group the elements based on that time. 懂就好.
```

1. **Assigning Timestamps**:

   按照eventTime处理, 每个流里面的record要有一个eventTimestamp assigned(毫秒单位), 一般就是element里面的某个filed.

   分配timestamp和生成watermark有两种方式

   1. **SourceFunction With Timestamps and Watermarks**

      Stream Source可以调用context方法分配timestamp到element上面, 然后再把它发给下流. 也可以发送watermarks. KafakSourceFunction它集成了TimestampAssigner, emmmm自己实现吧

      ```java
      @Override
      public void run(SourceContext<MyType> ctx) throws Exception {
      	while (/* condition */) {
      		MyType next = getNext();
              // 标记eventTime用collectWithTimestamp
      		ctx.collectWithTimestamp(next, next.getEventTimestamp());
      		if (next.hasWatermarkTime()) 
                  // 发送watermark用emitWatermark()
      			ctx.emitWatermark(new Watermark(next.getWatermarkTime()));
      	}
      }
      ```

   2. **Timestamp Assigners / Watermark Generators**

      Timestamp Assigner接收流, 标记好timestamp之后generate新的流, 会覆盖接收的流里面的watermark和timestamps. 一般放在source后面, 其他情况也有放在Map和Filter之后. 

      ```java
      // 拿到流了, 可以filter一下(也是拿到流), 使用assignTimestampsAndWatermarks方法把自己实现的assiger放进去, 生成新的流.
      DataStream<MyEvent> withTimestampsAndWatermarks = stream
              .filter( event -> event.severity() == WARNING )
              .assignTimestampsAndWatermarks(new MyTimestampsAndWatermarks());
      
      ```

      1. **With Periodic Watermarks**: `AssignerWithPeriodicWatermarks`  这个接口可以做周期的.
      2. **With Punctuated Watermarks**: `AssignerWithPunctuatedWatermarks` 这个接口可以修改element的timestamp

   3. **Timestamps per Kafka Partition**

      上面说的, kafkasource它自己实现的....

      Topic下每一个partition可能有自己的event time pattern, 可以使用 Kafka-partition-aware watermark generation, 然后watermarks在KafkaConsumer之内生成, 每一个partition的watermarks 被 merge.

      TODO 没看太明白, 说下一节会写.![Generating Watermarks with awareness for Kafka-partitions](Flink-Doc-1.assets/parallel_kafka_watermarks.svg)

##### 1.2 Builtin Watermark Generators (内置的watermark生成器)

上一节讲了可以通过实现接口去自定义assign给消息timestamps和自己定义提交watermarks. 但是为了更方面, flink带来了一些自己实现的timestamp assigners.本小节就介绍一下. 可以开箱即用, 也可以给自定义做借鉴.

1. **Monotonously Increasing Timestamps**(单调递增的timestamps)

   周期性watermark创建的最简单的例子就是 在records的timestamps是递增的. 当前的message的timestamp就可以当作watermark. 

   当然, 这种递增只需要在自己的并行度里面递增就好了. 比如说Kafka为源把, timestamp只需要在每个partition内是递增的就好了, 在stream做那种connect/union/merge/shuffle什么的时候, flink的watermark merging mechanism(机制) 会自己生成正确的watermark的.

2. **Fixed Amount of Lateness**(允许固定数量迟到msg的那种情况)

   周期性watermark 创建另一个例子就是watermark落后流过的最大timestamp一定的时间. 这种情况就是最大延迟了. 这种情况cover了流当中最大延迟的情况. 

   todo 后半段我看不懂了. 囫囵吞枣了, 用到自定义watermark的时候吧, 现在好像没有watermark之类的计算.



#### 2. State & Fault Tolerance (state和容错)

##### 2.0 Overview

本节主要介绍Flink提供的状态编程API, 状态流处理在Concepts里面介绍过

本节里面会讲:

1. 怎么使用state, 区分几种不同的state
2. 如何连接broadcastStream和non-broadcastStream, 并用state在其中交换信息
3. 怎么配置checkpoint用域容错
4. 怎么在runtime的时候从flink之外拿到state
5. 介绍state type怎么进化的
6. 讨论一下怎么实现自定义的序列化, 尤其是序列化的进化.

##### 2.1 Working with State

1. **Keyed DataStream**

   用KeyedState, 首先用.keyBy(keySelector)来指定datasource里面的一个key用来分类state(也分类recods themselves), 指定之后就变成了keyedDataStream, 然后就可以操作keyedState了

   keySelector顾名思义啊, 就是用record自定义一个key.

   Flink的数据模型不是建立在k-v对上面的, 所以我们不需要吧record编程key-value, 这里指定的key是虚拟的, 可以理解为建立在data上面的function.

   - Tuple Keys 和 Expression keys

     这两种key是两个特殊key, 他们是用tupleFields索引, 和expression来指定field, 现在不被建议使用了. tuple就像一个wapper类, 包含着n多个file.

2. **Using Keyed State**

   keyedState 接口提供了对所有类型state的access在当前这个element的key存在的生命周期里, 者意味着keyedState只能在KeyedStream里面用.

   现在, 我们先看不同类型的state有哪些, 然后在看怎么用它们.

   1. ValueState<T>: 这个state就存一个value, 可以更新可以读取(它的scope是上面说的key的scope里).
   2. ListState<T>: 存一个list的elements, 可以追加element, 也可以遍历.也可以更新
   3. ReducingState<T>: 存着一个代表所有添加到这个state的value的聚合, 类似于listState, 但是add之后就把所有元素使用ReduceFunction来变成一个聚合了.
   4. AggregatingState<IN, OUT>: 这个state存1个value, 这个value也是所有add进来的value的聚合, 但比ReducintState, 聚合类型可以于element的类型不同, 使用AggregateFunction执行聚合吧In转成OUT类型的聚合结果.
   5. MapState<UK, UV>: 存a list of mapping, 往里面存kv, 也可以遍历, 就和Map的接口差不多的功能.

   上面这五种state可以用clear()方法清除当前Key下面的state.(这个key是当前input进来的element的key)

   > 感觉就是, 在KeyBy()之后生成的stream里面, 可以有五种state, state 存这存那, 也可以清除.都是key为维度(key的scope就是state的scope)的.

   上面五种stateObj 是state的操作obj, 真正的state可以存在内存里, 也可以存在硬盘里/云上whereever. 然后第二个需要明白的是 state的value是按照element的key分的, 

   要拿到一个state handle就要创建一个stateDescriptor(state描述符), 这个描述符holds state的name, 也hold state的value的type, 也可以用户自定义function, 比如ReduceFunction. 这个stateDescriptor有valueStateDescriptor/list/reducing/map

   state可以通过RuntimeContext 拿到, 实现了RichFunction的function里就能拿到RuntimeContext.

   - **State Time-To-Live(TTL)**

     每种state都可以设置过期时间, 时间到了之后 state就会被clean up on best effort basis(尽力清理掉)

     map和list的state可以设置每个entry的TTLs.

     需要build一个stateTtlConfig obj, 然后TTL可以通过stateDescriptor开启(把stateTtlConfig传进去)

     ```java
     StateTtlConfig ttlConfig = StateTtlConfig
         .newBuilder(Time.seconds(1))
         .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
         .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
         .build();
         
     ValueStateDescriptor<String> stateDescriptor = new ValueStateDescriptor<>("text state", String.class);
     stateDescriptor.enableTimeToLive(ttlConfig);
     ```

     stateTtlConfig 可以设update的时间, 默认OnCreateAndWrite. 

     **notes:**

     1. state 需要存储上次修改的timestamp, 提高state的存储空间, 在Heap中的state附加一个Javaobj连接stateObj和timestam, RocksDB存储的state每个value需要多存8bytes
     2. TTL只支持以ProcessingTime为基准的.
     3. 如果使用支持TTL的stateDescriptor去恢复没有TTL的state会抛出兼容错误StateMigrationException
     4. TTL不属于Checkpoint/Savepoint, 知识当前flink运行的一个小功能.
     5. 只要valueSerializer能序列化null, 那么TTL的mapState就支持null做value. 不支持就用NullableSerializer包装一下null.

   - **Cleanup of Expired State(过期state怎么清理)**

     默认情况下过期的value在read的时候清理, 比如ValueState#value, 也可以在StateTtlConfig配置周期的GC. 

     Heap state 依赖invremental 清理, RocksDB state backend 使用compactive filter(压缩过滤器)来后台清理

     **1. cleanup in full snapshot**: 在take full state snapshot的时候可以启动清理来减小size, 从snapshot恢复的时候localState不会清理, 但不会把过期的state也恢复. 也需要在StateTtlConfig里面配置: 但是不适用在RocksDB state的增量checkpoint.??? todo: 之后再了解snapshot和checkpoint的关系.

     ```java
     StateTtlConfig ttlConfig = StateTtlConfig
         .newBuilder(Time.seconds(1))
         .cleanupFullSnapshot() // 在做snapshot的时候会清理.
         .build();
     // 清理策略可以开启/关闭在flink job runtime的时候.
     ```

     **2. incremental cleanup:** 也可以trigger 一些不断增加的state entries的cleanup, 这个trigger可以在每次拿state的时候使用. 在某个state正在cleanup的时候, storageBackend是懒遍历的, 就是先让iterator开始, 然后在过程中cleanup.

     ```java
      StateTtlConfig ttlConfig = StateTtlConfig
         .newBuilder(Time.seconds(1))
         .cleanupIncrementally(10, true) // 每次trigger之后检查10个entry, true在每次record process的时候trigger一下cleanup.默认不是这样的, 默认不会每次process的时候cleanup.
         .build();
     ```

     1. **Note:**
     2. 也就是如果没有record process, 也没有 access to state, 那么就不会清理, 过期也就在那放着
     3. 开启incremental cleanup会增加latency哦
     4. intemental cleanup 只在Heap state backend的时候实现了这个功能, RocksDB没这个.
     5. 既是heap state又是正在同步snapshotting的时候, global iterator会留着所有key的copy(占内存哦), 因为state不支持并发修改. 异步的snapshot就没这个问题
     6. cleanup strategy(清理策略)可以热更新哦

     **3. Cleanup during RocksDB compaction(在RocksDB压缩的时候cleanup)**: 在RocksDB的时候, Flink自己的一个压缩filter会在后台运行, 目的就是cleanup. 





### 六. Batch

> Flink DataSet API 批处理编程
>
> 

### 七. Table API & SQL

### 八. Data Types & Serialization

### 九. 执行管理/ 库/ Migration





















