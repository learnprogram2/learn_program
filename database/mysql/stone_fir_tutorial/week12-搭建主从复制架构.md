### 118-119: MySQL主从复制架构为什么必要

**MySQL在生产应用, 必要搭建主从复制架构, 基于一些工具实现的高可用. 还可以做读写分离, 如果再高的并发, 需要用中间件实现分库分表.**

- 主从节点数据基本一致, 进而实现HA

<img src="week12-%E6%90%AD%E5%BB%BA%E4%B8%BB%E4%BB%8E%E5%A4%8D%E5%88%B6%E6%9E%B6%E6%9E%84.assets/118.%20%E4%B8%BB%E4%BB%8E%E6%9E%B6%E6%9E%84%E5%AE%9E%E7%8E%B0HA" alt="image-20201129143651822" style="zoom:50%;" />

- **读写分离**

  <img src="week12-%E6%90%AD%E5%BB%BA%E4%B8%BB%E4%BB%8E%E5%A4%8D%E5%88%B6%E6%9E%B6%E6%9E%84.assets/image-20201129144221791.png" alt="image-20201129144221791" style="zoom:50%;" />

  为什么要做读写分离: 16G8核单机最多承受4k读写请求, 压力太大需要区分开.

  可以挂多个从

#### 主从同步的原理-binlog同步

![image-20201129150919879](week12-%E6%90%AD%E5%BB%BA%E4%B8%BB%E4%BB%8E%E5%A4%8D%E5%88%B6%E6%9E%B6%E6%9E%84.assets/image-20201129150919879.png)





### 121-124. 搭建MySQL主从复制架构

#### 121. 异步复制

1. 主库打开binlog功能. 创建用于主从复制的MySQL账号.

2. 使用mysqldump工作为master做全量备份

   `..../mysql/bin/mysqldump --single-transaction -uroot -proot --master-data=2 -A > backup.sql`

   --master-data=2: sql里面记录此时master的binlog文件地址和position.

3. 拿到backup.sql直接到slave执行.

4. slave上面执行复制命令:

   `CHANGE master to master_host='xxx.xx.xx.xx', master_user='xx', master_password='xx', master_log_file='binlog文件名', master_log_pos=xxx;`

   

#### 122. 半同步复制

MySQL要保证可靠性, 保证数据安全, 常用的是半同步复制, 保证至少binlog写入.

- `AFTER_COMMIT`: 日志写入binlog, 等待binlog复制到slave之后就可以提交事务
- `AFTER_SYNC(默认)`: binlog里的命令要等待slave响应确认信息才提交事务.

安装:

1. 先搭建好异步复制的主从架构

2. master中安装半同步复制插件, 开启半同步复制功能

   ```mysql
   install plugin rpl_semi_sync_master soname 'semisync_master.so'
   set global repl_semi_sync_master_enabled=on;
   show plugins;
   ```

3. slave中安装slave版的插件, 并且开启

   ```mysql
   install plugin rpl_semi_sync_slave soname 'semisync_slave.so'
   set global repl_semi_sync_slave_enabled=on;
   show plugins;
   ```

4. 重启从库的IO线程:

   stop slave io_thread; start slave io_thread; 

5. 检查半同步复制是否正常:

   ```mysql
   show global status like '%semi%';
   # 看 Rpl_semi_sync_master_status是不是on
   ```

   

#### 123. GTID搭建方式

GTID复制和传统复制两种方式.只是写了步骤, 没有深入讲解...









### 124. 如何解决主从复制的数据延迟问题

- `percona-toolkit`工具集的`pt-heartbeat`工具, 可以测试主从延迟. 

  在主库创建heartbeat表, 定时更新时间戳, 在从库上monitor线程检查时间差.

- **从库多线程复制数据**

  设置`slave_parallel_workers>0` 然后把`slave_parallel_type`设置成`Logical_Clock` 

- **中间件强制取消读写分离**, 读写都在master上.

- 调整半同步策略, 按照数据安全要求选择after_commit还是after_sync.







