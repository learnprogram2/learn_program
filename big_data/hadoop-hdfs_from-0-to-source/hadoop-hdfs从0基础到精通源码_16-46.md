## 搭建HDFS HA集群

### 16. 集群部署规划-逻辑架构图/物理架构图

ZKFC(zookeeper failover controller): 负责DR选举.

QuorumPeerMain: zk进程.

journal-node集群负责主备namenode之间同步editslog.

datanode存储数据, 之中有block做replicate, 实现HA.



### ![05_hdfs集群部署规划](hadoop-hdfs%E4%BB%8E0%E5%9F%BA%E7%A1%80%E5%88%B0%E7%B2%BE%E9%80%9A%E6%BA%90%E7%A0%81_16-46.assets/05_hdfs%E9%9B%86%E7%BE%A4%E9%83%A8%E7%BD%B2%E8%A7%84%E5%88%92.png)

### 17. 















## 21-23. hdfs的简单使用

### 21. 基于filesystem.shell操作hdfs.sz

























