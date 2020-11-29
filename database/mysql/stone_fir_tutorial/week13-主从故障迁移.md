### 125-127. 数据高可用: 基于主从复制实现故障迁移

在master down掉之后, 需要把slave切换成master

**本章讲解的是使用MHA架构, 监控和切换master**

- MHA: Master High Avaliability Manager and Tools for MySQL

  使用perl脚本编写的工具, 包括Manager节点和Node节点

  Node节点在MySQL服务器上, 监控和执行操作命令

  Manager负责调度和执行.

1. 保证所有机器是免密通讯的.