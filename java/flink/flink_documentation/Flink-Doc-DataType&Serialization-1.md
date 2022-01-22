[文章](https://arxiv.org/pdf/1506.08603.pdf)

# Deployment & Operations

## Cluster & Deployment

### 0. Overview

flink有很大的操作空间来决定Flink怎么运行什么时候运行

#### 0.1 Deployment Modes: 

Flink的执行模式有: 不同之处就是cluster的生命周期和资源隔离的保证不同, 然后main方法在client或者在cluster执行
1. session mode
session mode假设已经有一个正在运行的集群, 使用集群的资源来执行提交进来的application. 同一个session cluster里面的应用互相使用和竞争相同的资源. 优点是不用为每个提交的job付出全部的资源. 但是, 一旦一个taskmanager里面的job有错误之后, TaskManager上所有的job都失败了.这个增加了job的风险, 也加重了jobManager的负担. 

2. Per-Job mode
旨在提供更好的资源隔离guarantee, _Per-Job_ 模式使用已有的集群管理框架(比如YARN, K8s)来给每一个提交的job启动一个cluster. cluster只是这一个job用, job完了之后, cluster就被干掉了, 相关的资源也干掉. 这个模式提供了更好的资源隔离, 一个失败的job只能把自己的TaskManager干掉. 将book-keeping的工作分在不同的JobManager上. 生产首选.

3. Application mode
    上面两个模式, application的`main`方法在client上面执行. 在本地执行应用的dependencies 下载. 执行main()方法可以提取出FLink的运行时候的JobGraph 并且把依赖发到cluster. 所以上面两种让client是一个重的资源消耗. 需要贷款去下载依赖和提交到cluster.
    _Application Mode_ 这个mode也为每个提交的application创建一个cluster. 但是main()方法在**JobManager**上运行.  创建的这个cluster可以看成是一个session-cluster, 这个但是只在一个application里面的jobs里面共享, 在application完成后就干掉. 因为这个架构, _application Mode_ 提供了和`Per-Job`模式相同的资源隔离和loadBalance保证, 但是是以整个Application的粒度.  在JobManager运行main()方法节省CPU和贷款. 因为只有一个JobManager, 所以更均匀的分散了下载集群里多个application下载以来的网络负载(((没理解这个, 原来的负载是放在taskmanager上面么?)

  相比`Per-Job`模式, `Application`模式允许一个application包含多个job. job执行的顺序不受部署模式的影响, 只受job调用的顺序. 调用用`execute()`方法, 是阻塞方法, 这个做完下个才做. 还有`executeAsync()`异步运行. 

  注意: application模式里面多个execute()方法执行不支持High Availability, 只有single-execute()支持HA.

4. Summary

  session模式, cluster的生命周期是所有的job运行的周期.

  per-job模式付出了每个job一个cluster的代价, 但是带来更好的资源隔离保证. 集群的生命周期绑定在了job上.

  application模式为每一个application创建一个sessionCluster, 而且在cluster内部执行application的main()方法.

#### 0.2 Deployment Targets

支持多种部署: `Local, Standalone, Yarn, Mesos, Docker, K8s`.



#### 0.3 供应商解决方案

有很多供应商提供了Flink全方位的管理. 但这些供应商不官方支持. 巴拉巴拉 有Alibaba, Amazon,...



#### 0.4 Deployement Best Practice

1. 怎么在classpath里提供依赖:

   Flink给Flink自己或者用户的application的愿意来提供了几种提供dependencies的方法(比如jar包, 或者static data). 根据部署的模式和目标不同, 但有一些共性: 

   - `lib/folder`下面的文件会被添加到启动Flink的classpath下面. 比较适合放一些libraries比如Hadoop或者不是插件的文件系统. 注意这里面添加的可能会扰乱flink的
   - `plugins/<name>/` 里面的在Flink的runtime被加载, 使用不同的classloaders避免和正在用的发生conflicts. 只有[plugin](https://ci.apache.org/projects/flink/flink-docs-release-1.11/ops/plugins.html)的jar才能放在这里哦

2. 在本地下载Maven依赖:

   如果使用maven依赖开发flink, 可以用pom文件去下载所需的依赖. 

   在运行`mvn package`命令时候在同一个目录下会创建`jar/`包

   

### 1. Local Cluster

#### 1.1 Setup: 下载和启动Flink

Flink运行在Linux和Mac上面, Window可以用`Cygwin or WSL` 运行, 只需要JDK就可以运行

1. 首先下载binary, 然后解压. 

2. `./bin/start-cluster.sh`启动flink.

   打开`localhot:8081`就是dashboard界面. 可以检查log目录下的日志

3. 调用`./bin/stop-cluster.sh`关机.



### 2. Standalone Cluster

这个讲解的是怎么在分布的静态集群上运行FLink

#### 2.1 要求

需要`Unix-like` 的环境, 把cluster由一个masterNode和n个workerNode. 安装JDK, SSH(使用flink脚本管理远程的component). 保证所有的node都有无密码的SSH, 相同的目录结构.

需要`JAVA_HOME`环境变量. 当然也可以在flink-conf.yaml设置`env.java.home`.

#### 2.2 Flink Setup

1. 先下载最新的版本, 放到masterNode里, 解压好.

2. [Configuring Flink](https://ci.apache.org/projects/flink/flink-docs-release-1.11/ops/config.html): 编辑`conf/flink-conf.yaml`文件

   `jobmanager.rpc.address`指向masterNode, 还要定义Flink最大内存, 通过`taskmanager/jobmanager.memory.process.size`之类的配置参数. 都是MB单位. 在各自node里面的配置文件里可以覆写.(???flink不是只放在了Master里面么

   下面的图介绍了设置三个node. 

   <img src="flink_11_doc_deployment_Operations.assets/image-20200826172522044.png" alt="image-20200826172522044" style="zoom:67%;" />

3. 启动Flink

4. 添加JobManager/taskManager到Cluster里.

   可以使用`bin/jobmanager.sh`和`taskmanager.sh`脚本添加JobManager和TaskManager

   ```shell
   bin/jobmanager.sh ((start|start-foreground) [host] [webui-port])|stop|stop-all
   bin/taskmanager.sh start|start-foreground|stop|stop-all
   ```

   

### 3. YARN/Mesos/Docker/K8s



### 4. Hadoop 集成

#### 4.1 提供 Hadoop classes

如果使用Hadoop功能, 需要提供FLink Hadoop类. 

建议的是通过`HADOOP_CLASSPATH`环境变量配置Hadoop的classpath. 





## JobManager High Availability

### Overview

JobManager 负责协调每一个Flink部署, 负责_scheduling_和_resource management_. 

默认的, 每个FlinkCluster有一个JobManager, 这会造成SPOF(*single point of failure*)问题: 如果jobManager挂掉, 不能提交新程序, 运行的程序也会失败. 

我们可以用JobManager的HA来恢复JobManager, 消除SPOF, 可以在standalone或者YARN集群里面配置HA. 

更多HA实现细节可以看[JobManager High Availability](https://cwiki.apache.org/confluence/display/FLINK/JobManager+High+Availability).



### 1. Standalone cluster HA

独立的cluster里面通用的JobManager的HA做法是有一个做leader的JobManager, 然后由多个standby的JobManager等着失败. 能供提供担保, 程序在standby的JobManager上任之后就接着运行. standby的和真正的jobmanager没有区别, 每一个都可以变成master或者standby.

<img src="flink_11_doc_deployment_Operations.assets/jobmanager_ha_overview.png" alt="img" style="zoom:50%;" />

#### Configuration

为了开启JobManager的HA, 必须借助于Zookeeper, 配置ZooKeeper Quorum(仲裁), 设置一个master file里面有所有jobmanager的host和webUI的port.

Flink使用ZK为所有running JobManager进行coordinate. ZK提供高可用的coordination通过leader选举和清凉的state存储. 文章下面有**Flink包含启动一个简单的zk的脚本.** 

1. **Masters File(masters)** `conf/masters`

   在文件里面添加jobmanager的地址:

   ```text
   jobManagerAddress1:webUIPort1
   [...]
   jobManagerAddressX:webUIPortX
   ```

   默认的, jobManager都是随机端口, 可以通过`**high-availability.jobmanager.port**`这个key指定, 50000-50025之间. 

2. **config File(flink-conf.yaml)**

   ```shell
   # 1. 开启HA模式:
   high-availability: zookeeper
   # 2. ZK Quorum(仲裁) 配置zk的地址
   high-availability.zookeeper.quorum: address1:2181[,...],addressX:2181
   # 3. ZooKeeper root: 要制定clusterNodes存放的根目录
   high-availability.zookeeper.path.root: /flink
   # 4. ZK的cluster-id. 这个id的zkNode里面存放着cluster所需要的coordinate 数据
   high-availability.cluster-id: /default_ns # important: customize per cluster
   # 注意: 在YARN的时候不要手动设置这个cluster-id.因为cluster-id会根据applicationID自动生成. 手动指定会覆写.
   # 5. Storage directory(required): Jobmanager的metadata在文件系统里面存着, zk里面存着一个pointer. 在recover的时候要用到. 
   high-availability.storageDir: hdfs:///flink/recovery
   ```

   配置好了之后, 就可以运行了. 会启动一个HA-cluster. zk仲裁必须启动时候调用script保证配置了cluster的rootPath.

3. Example: Standalone Cluster with 2 JobManagers

   ```text
   1. 配置HA模式, 还有zk仲裁的参数在conf/flink-conf.yaml
   high-availability: zookeeper
   high-availability.zookeeper.quorum: localhost:2181
   high-availability.zookeeper.path.root: /flink
   high-availability.cluster-id: /cluster_one # important: customize per cluster
   high-availability.storageDir: hdfs:///flink/recovery
   2. conf/masters文件里把所有准备好的JobManager都配上
   localhost:8081
   localhost:8082
   3. 在conf/zoo.cfg里配置zkserver. 只支持每个机器run一个zkServer
   server.0=localhost:2888:3888
   4. 开启zk仲裁:
   $ bin/start-zookeeper-quorum.sh
   Starting zookeeper daemon on host localhost.
   5. 开启HA-Cluster
   $ bin/start-cluster.sh
   Starting HA cluster with 2 masters and 1 peers in ZooKeeper quorum.
   Starting standalonesession daemon on host localhost.
   Starting standalonesession daemon on host localhost.
   Starting taskexecutor daemon on host localhost.
   6. 关闭ZK仲裁, 关闭HA-Cluster
   $ bin/stop-cluster.sh
   $ bin/stop-zookeeper-quorum.sh
   ```

   

#### YARN Cluster的HA

YARN集群的时候, 不需要运行多个JobManager实例, failure之后就restart就好了. 

1. Configuration: 

   ```shell
   # 1. 最大的Application Master attempts, 配置在`yarn-site.xml`里面
   <property>
     <name>yarn.resourcemanager.am.max-attempts</name>
     <value>4</value>
     <description>
       The maximum number of application master execution attempts.
     </description>
   </property>
   # 2. Application attemps, 配置在`flink-conf.yaml`里面
   yarn.application-attempts: 10
   说明应用会尝试重启9次, 然后才会YARN fail这特application. 
   ... 用的时候再说把
   ```

   

#### Configuring For Zookeeper Security

zk运行在Kerberos里面的secure模式, 可以覆写zk的配置在`flink-conf.yaml`里面

```text
zookeeper.sasl.service-name: zookeeper     # default is "zookeeper". If the ZooKeeper quorum is configured
                                           # with a different service name then it can be supplied here.
zookeeper.sasl.login-context-name: Client  # default is "Client". The value needs to match one of the values
                                           # configured in "security.kerberos.login.contexts".
```



#### Zookeeper Versions

Flink包含着zk client 从3.4到3.5. 3.4在lib下面是默认的, 3.5在opt下面, SSL安全连接. 哪个放在lib下面就用哪个.



#### Bootstrap ZK

如果我们没有运行的zk实例, 可以用Flink内带的文件. `conf/zoo.cfg`里面有zk的配置模板. 可以指定host运行zk

```java
server.1=localhost:2888:3888
# server.2=host:peer-port:leader-port
```

`bin/start-zookeeper-quorum.sh`这个脚本会启动一个zk集群 通过一个Flink的包装. 如果生产实践里, 最好是自己有一个ZK集群.





## State & Fault Tolerance 容错

### Checkpoints

#### Overview

checkpoints通过允许state和从流里面指定的位置恢复, 让application有了failure-free执行的语义, 保证了容错.

#### 保存 checkpoint

checkpoint默认不会保留, 只会用于job的故障恢复. 在job cancell的时候就被干掉了. 当然可以设置保留checkpoint. 在job被canceled的时候就不会清理, 我们也可以从checkpoint里面恢复

```java
CheckpointConfig config = env.getCheckpointConfig();
config.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
```

1. checkpoint的**Directory Structure**

   通过`state.checkpoints.dir`配置参数可以指定存储地方, 存储的目录大概是:

   ```shell
   # 1. 配置
   state.checkpoints.dir: hdfs:///checkpoints/
   env.setStateBackend(new RocksDBStateBackend("hdfs:///checkpoints-data/"));
   # 2. 目录
   /user-defined-checkpoint-dir
       /{job-id}
           |
           + --shared/	# 这个目录里面多个checkpoint的state
           + --taskowned/  #放jobmanager里面不会drop的state
           + --chk-1/
           + --chk-2/
           + --chk-3/
           ...
   ```

2. **Checkpoint和savepoint的不同:**

   checkpoint使用的是statebackend里面的特殊数据format, 是可增加的

   checkpoint不支持Flink的特殊功能(rescalling之类的(

3. 从保存的checkpoint里面恢复

   ```shell
   $ bin/flink run -s :checkpointMetaDataPath [:runArgs]
   ```

   

#### Unaligned checkpoints

Flink1.11里面可以不对齐做checkpoint了. 不对齐需要包含一些in-flight的data(buffer里面的) 作为checkpoint的state.这就可以允许checkpoint的barrier越过buffer了. 

如果checkpoint时间太长了我们就应该开启这个. 开启了之后checkpoint 时间和latency独立开来. 注意部队其对增加IO压力, 所以需要考虑一下.

这个功能比较新, 然后有一些限制: 

- 从unaligned的checkpoints里面恢复不能rescale. 需要savepoint.savepoint是必须对齐的
- flink不支持冰心的不对齐checkpoint. 但如果更快了, 也不需要并发checkpoint了
- 不对齐的checkpoint默认支持watermark在恢复的时候. 只是implicit(隐含)的

recovery的第一步就开始生成watermark了, 而不是保存最后一个watermark来为了rescaling. 在不对其的checkpoints回复的时候, Flink在恢复了in-flight的数据之后就生成watermarks. 如果我们有对最后一个watermark里的data操作, 会产生比对齐checkpoint更多的结果. 为了保证最后一次watermark的操作, 需要把每一个key-group放在不同的union-state里. 1.11.0里面没实现.

未来Flink会让不对齐的checkpoint变成默认的. 解决了其他的问题之后.



### SavePoints

#### 1. 什么是savepoint, 和checkpoint有什么不同

savepoint是当前执行state的快照, 通过[checkpoint机制](https://ci.apache.org/projects/flink/flink-docs-release-1.11/learn-flink/fault_tolerance.html) 创建. 可以用来重启升级我们的FlinkJob. Savepoint包含两个部分: 在可靠存储里面的很大的二进制文件目录, 还有一个小的元数据文件(包含指针指向文件). 

从概念上讲savepoint和checkpoint不同, **有点类似于DB里面的backup和recoveryLog之间的不同**.  checkpoint的主要目的是在job失败的时候提供recovery机制. checkpoint的生命周期由Flink掌管. checkpoint比较轻量还有快速恢复.  

Savepoint被用户创建掌管和删除. 成本更高. 

除了上面的不同, 现在Checkpoint和savepoint的实现基本使用的相同的code, 相同的类型. 未来可能引入不同. 有一个例外是用RockesDB做checkpoint, 使用RocksDB内部的格式而不是flink的savepoint格式, 这样可以让checkpoint更轻量.

#### 2. 绑定OperatorID

建议调整描述我们的应用通过operatorID. 这些ID就是state的scope.

```java
stream.uid("source-id") // ID for the source operator
```

如果不指定ID就自动生成, 只要ID没变就可以自动的从savepoint恢复. 自动生成的ID依赖于我们的structure, 所以最好手动指定

- Savepoint state

  我们可以把savepoint想象成是一个map, 存折每一个stateful operator的state

  ```text
  Operator ID | State
  ------------+------------------------
  source-id   | State of StatefulSource
  mapper-id   | State of StatefulMapper
  ```

  

#### 3. Operations 操作

我们可以trigger 一个savepoint通过command line client(CLi), 也可以用webUI来做. 



#### 4. FAQ

1. 应该为每个operator都标记ID么: 是的.虽然只有statefulOperator才会做savepoint, 但是Flink内置的operator(windowOpeartor)都是stateful的.
2. 如果添加一个新的stateful的Operator会怎么样: savepoint里面没有她的state, 和stateless的operator一样.
3. 如果删除一个有state的operator会怎么样: 默认会失败. 可以启动命令里添加`--allowNonRestoreState`

4. 如果我reorder stateful的operator怎么办? 标记了ID之后会正常重启. 没有标记, 会自动生成新的ID, 然后就恢复不了了

5. 如果添加或删除或者reorder 没有state的operator怎么办?

   如果标记ID了就正常恢复

   没有标记ID, 就完了, savepoint失效了

6. 如果修改了并行度会怎么恢复?

   1.2之后可以制定新的并行度完美的重启

   小于1.2或者使用了废弃的API, , 要把我们的job和savepoint[迁移](https://ci.apache.org/projects/flink/flink-docs-release-1.11/ops/upgrading.html)到1.2以后才能修改并行度. 

7. 可以自己挪动savepoint文件么?

   不能. 因为元数据file里面的指针会失效. 但是, 可以修改metadataFile里面的指针. 或者可以用`SavepointV2Serializer`做savepoint的读写. 



### State Backends

DataStreamAPI里面的state用不同的形式保管着state

```java
1. windows 需要在state里面存折element指导trigger;
2. 有k-vstate的function存储state;
3. 实现CheckpointFunction的需要容错的数据.
```

stateBackend决定了我们怎么存储和使用state.

#### 1. Available State Backends

有三个stateBackend

- MemoryStateBackend

  在JavaHeap里面存着, kvstate和windowOperator存折hashtable, 里面放着Values, triggers之类的.

  到了Checkpoint的时候, statebackend会做state的快照, 然后把它作为checkpoint ACK message的一部分发送给JobManager, **JobManager存着checkpoint** 

  memoryStateBackend可以配置成异步的快照, 强烈推荐. 默认开启. 如果要关闭, 可以实例化一个MemoryStateBackend然后设置成false.

  ```java
  new MemoryStateBackend(MAX_MEM_STATE_SIZE, false);
  ```

  MemoryStateBackend的局限: 

  1. 每个单独的state默认小于5MB
  2. state必须小于akka通讯的栈帧大小
  3. 聚合的state必须小于JobManager内存大小















