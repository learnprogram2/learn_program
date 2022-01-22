## 搭建HDFS HA集群

### 16. 集群部署规划-逻辑架构图/物理架构图

ZKFC(zookeeper failover controller): 负责DR选举.

QuorumPeerMain: zk进程.

journal-node集群负责主备namenode之间同步editslog.

datanode存储数据, 之中有block做replicate, 实现HA.



### ![05_hdfs集群部署规划](hadoop-hdfs%E4%BB%8E0%E5%9F%BA%E7%A1%80%E5%88%B0%E7%B2%BE%E9%80%9A%E6%BA%90%E7%A0%81_16-46.assets/05_hdfs%E9%9B%86%E7%BE%A4%E9%83%A8%E7%BD%B2%E8%A7%84%E5%88%92.png)

### 17. 

1. 安装三台zk, 部署zk集群

2. 安装hadoop
   1. 编辑vi core-site.xml
   2. 编辑vi hdfs-site.xml

```log
[root@hadoop01 sbin]# ./start-dfs.sh 
Starting namenodes on [hadoop01 hadoop02]
Last login: Wed Mar 24 13:17:24 EDT 2021 on pts/0
Starting datanodes
Last login: Wed Mar 24 13:20:55 EDT 2021 on pts/0
localhost: datanode is running as process 3625.  Stop it first.
Starting journal nodes [hadoop05 hadoop04 hadoop03]
Last login: Wed Mar 24 13:20:58 EDT 2021 on pts/0
hadoop04: journalnode is running as process 3539.  Stop it first.
hadoop05: journalnode is running as process 3529.  Stop it first.
hadoop03: journalnode is running as process 3533.  Stop it first.
Starting ZK Failover Controllers on NN hosts [hadoop01 hadoop02]
Last login: Wed Mar 24 13:21:06 EDT 2021 on pts/0
hadoop01: zkfc is running as process 4314.  Stop it first.
hadoop02: zkfc is running as process 3488.  Stop it first.

```













## 21-23. hdfs的简单使用

### 21. 基于filesystem.shell操作hdfs.sz

























