### 01. 基于MySQL和Oracle的数据分析平台

数据分析需求: 每日热销商品, 流量消耗...

2014年以前, 数据库主要用Oracle/MySQL里. SSH+Oracle框架开发. 使用大SQL定时查询出报表.

<img src="hadoop-hdfs%E4%BB%8E0%E5%9F%BA%E7%A1%80%E5%88%B0%E7%B2%BE%E9%80%9A%E6%BA%90%E7%A0%81.assets/01_%E5%9F%BA%E4%BA%8E%E6%95%B0%E6%8D%AE%E5%BA%93%E7%9A%84%E5%88%86%E6%9E%90%E5%B9%B3%E5%8F%B0.png" alt="01_基于数据库的分析平台" style="zoom: 67%;" />

### 2. 大数据出现的原因: 数据量激增

大数据的需求来源: **数据报表的需求** 

- **存储:**

  **网页埋点, 记录用户行为日志**.  各种行为. 1亿条数据, 每条100byte, 9.3G.

  所以后期 去IOE(IMB小型机,Oracle, EMC的存储设备).

  - Orcale太贵, MySQL分库分表太麻烦.

- **计算:** 太慢



### 3. hadoop和其他大数据技术的诞生和发展

产生: 因为数据量大, 计算慢. 2013开始产生大数据概念, 初始应用.

> **分布式是大数据的本质:** 分布式的计算, 存储, 搜索, 调度.
>
> **离线批处理:** 基于flume, yarn+mapreduce+hdfs, hive 的分析计算 是离线批处理. 

1. **hadoop:** hdfs+yarn+mapreduce
   - **HDFS: hadoop-distribute-fileSystem. 分布式存储系统.**
   - **mapreduce: 分布式计算, 大任务分解创建多个计算任务.**
   - **yarn: 分布式资源调度: 负责计算任务的分发.**

4. **Hive:** 

   **把SQL基于mapreduce的API翻译代码**, 让mapreduce接收SQL计算任务. 解放大家.

5. **flume:**

   **分布式日志采集.**

<img src="hadoop-hdfs%E4%BB%8E0%E5%9F%BA%E7%A1%80%E5%88%B0%E7%B2%BE%E9%80%9A%E6%BA%90%E7%A0%81.assets/02_hadoop%E7%9A%84%E8%AF%9E%E7%94%9F.png" alt="02_hadoop的诞生" style="zoom: 50%;" />

6. hbase: 

   **分布式NoSQL数据库.** 基于HDFS存储实现, 封装的NoSQL数据库, 毫秒级CRUD.

7. spark:

   分布式计算, 替代mapreduce计算. spark的代码生成计算任务, 通过yarn分配到机器.

   spark SQL, spark streaming, spark mllib(分布式存储数据, 机器学习.)

8. elasticsearch: 

   分布式存储+搜索.

9. kylin: 

   分布式OLAP分析 online-analyze-process



### 4. 离线计算和实时计算, 以及与Hadoop的关系

- **离线计算:** 批量的概念. 

  flume批量导入.

- **实时计算:** 流的概念.

  Kafka实时接入.

- hdfs: 分布式存储:

  在离线/实时计算里都作为存储基础.

<img src="hadoop-hdfs%E4%BB%8E0%E5%9F%BA%E7%A1%80%E5%88%B0%E7%B2%BE%E9%80%9A%E6%BA%90%E7%A0%81.assets/03_%E7%A6%BB%E7%BA%BF%E8%AE%A1%E7%AE%97%E5%92%8C%E5%AE%9E%E6%97%B6%E8%AE%A1%E7%AE%97.png" alt="03_离线计算和实时计算" style="zoom:67%;" />

### 5. HDFS图解1: 分布式存储_整体架构

![04_hdfs整体架构原理](hadoop-hdfs%E4%BB%8E0%E5%9F%BA%E7%A1%80%E5%88%B0%E7%B2%BE%E9%80%9A%E6%BA%90%E7%A0%81.assets/04_hdfs%E6%95%B4%E4%BD%93%E6%9E%B6%E6%9E%84%E5%8E%9F%E7%90%86.png)

### 6. HDFS图解2: master-slave模式的分布式架构

1. **datanode:** 存放每份block数据文件.(128MB一个block)

2. **namenode:** 元数据,同时备份磁盘.  根据元数据存储的block信息, 响应client的请求. 

   **client根据元数据去datanode里面拿数据.**

   <img src="hadoop-hdfs%E4%BB%8E0%E5%9F%BA%E7%A1%80%E5%88%B0%E7%B2%BE%E9%80%9A%E6%BA%90%E7%A0%81.assets/06_master-slave%E6%A8%A1%E5%BC%8F%E7%9A%84%E5%88%86%E5%B8%83%E5%BC%8F%E6%9E%B6%E6%9E%84.png" alt="06_master-slave模式的分布式架构" style="zoom:67%;" />

### 7. HDFS图解3: 文件系统元数据的管理机制

- **文件系统元数据:** 

  文件层级结构, 目录内的文件信息, 文件的block信息...

- **元数据支持HDFS可以像Linux文件系统一样易用.

![07_文件系统元数据的管理](hadoop-hdfs%E4%BB%8E0%E5%9F%BA%E7%A1%80%E5%88%B0%E7%B2%BE%E9%80%9A%E6%BA%90%E7%A0%81.assets/07_%E6%96%87%E4%BB%B6%E7%B3%BB%E7%BB%9F%E5%85%83%E6%95%B0%E6%8D%AE%E7%9A%84%E7%AE%A1%E7%90%86.png)





### 8. HDFS图解4: hadoop1.x中的 SecondaryNameNode

**SecondaryNameNode: **

1. **负责checkpoint操作:** 解决日志文件size问题.

- 拉取editlog和fsimage
- 压缩editLog和fsimage, 生成新的fsimage
- 放回fsimage.

2. **namenode数据的备份**

   <img src="hadoop-hdfs%E4%BB%8E0%E5%9F%BA%E7%A1%80%E5%88%B0%E7%B2%BE%E9%80%9A%E6%BA%90%E7%A0%81.assets/08_SecondaryNameNode.png" alt="image-20201115201128547" style="zoom:50%;" />

3. 1.0.4版本后**改名为checkpoint-node**.





### 9. HDFS图解5: hadoop1.x中的 backup-node

**目的:** 为了优化掉checkpoint-node需要pull和push editLog+fsimage 的操作.

**解决思路:** namenode中的元数据直接输出给backup-node, 由backup-node负责维护editlog和fsimage.

**优点:** 原来的checkpoint-node在两个checkpoint之间的数据不能保证, 现在可以保证了.

<img src="hadoop-hdfs%E4%BB%8E0%E5%9F%BA%E7%A1%80%E5%88%B0%E7%B2%BE%E9%80%9A%E6%BA%90%E7%A0%81.assets/09_backup-node.png" alt="image-20201115202202650" style="zoom:50%;" />





### 10. HDFS图解6: hadoop2.x 双实例实现 HA

1. journal-node集群负责同步editslog数据.
2. 双namenode上面各启动一个ZKFC(zookeeper failover controller)进程: 由health monitor负责监控master

<img src="hadoop-hdfs%E4%BB%8E0%E5%9F%BA%E7%A1%80%E5%88%B0%E7%B2%BE%E9%80%9A%E6%BA%90%E7%A0%81.assets/10_双实例实现 HA.png" alt="image-20201115211606059" style="zoom:50%;" />



### 11. HDFS图解7: 双实例的元数据管理

**由standby的实例, 运行checkpointThrid线程, 负责做checkpoint.**

![image-20201115211746960](hadoop-hdfs%E4%BB%8E0%E5%9F%BA%E7%A1%80%E5%88%B0%E7%B2%BE%E9%80%9A%E6%BA%90%E7%A0%81.assets/11_双实例的元数据管理.png)





### 12. HDFS图解8: 超大文件的文件存储机制

上面是namenode的元数据管理, 现在是datanode的数据管理:

- datanode在磁盘里维护blocks. 每个block有固定大小. 默认128m.
- namenode负责均分配block
- dananode把自己的block信息, blockReport+心跳给namenode. 



### 13. HDFS图解9: 数据容错: 副本机制

**replication factor:** 副本数量参数, 默认3.

**rackAware机架感知+pipeline式上传:** 

namenode告诉client接收数据的三个datanode, 第一个datanode接收数据block, 把block传输给同机架的机器副本一份. 副本datanode再传输给不同机架的datanode副本第二份.

![image-20201115221209345](hadoop-hdfs%E4%BB%8E0%E5%9F%BA%E7%A1%80%E5%88%B0%E7%B2%BE%E9%80%9A%E6%BA%90%E7%A0%81.assets/13_数据容错-副本机制.png)





### 14. HDFS图解10: 安全模式是什么

- **namenode启动进入safe-mode.** 
- **HDFS等候各个datanode的心跳+blockReport.**
- **block-report合格之后退出. 80%的副本要可用等等.**



### 15. HDFS图解11: 集群节点容错机制

- 集群节点故障:
  - 网络分区: network-partition: 失联了. 心跳检测发现.
  - 数据破损: HDFS数据完整性校验: 每个文件都有checksum.

- 元数据损坏:

  editslog和fsimage: HA双机保证.



