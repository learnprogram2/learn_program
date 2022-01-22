## Application Development 0

>  **Project Build Setup & Basic API Concepts**
>
>  这部分是1.10的文档, 1.11就没有了



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



