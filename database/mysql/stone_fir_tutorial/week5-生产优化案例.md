

## Week5: 生产优化案例

> 之前的几周, 吧物理数据结构和Buffer Pool缓存结合起来理解就好. 
>
> 本周介绍生产优化案例



### 33. MySQL的**日志顺序读写**, 和 **数据文件随机读写** 原理.

> 本小节先讲: MySQL数据可和底层操作系统之间的交互原理. 

1. MySQL有两种数据读写机制:

   - redo-log, binLog的日志顺序读写:

     顺序读性能很高(和内存随机读写差不多) **读写数据吞吐量**是性能关键.

   - 磁盘文件的数据页, 磁盘随机读写: 比如CRUD加载数据页是随机读写

     TODO 因为要读取的数据也可能在磁盘任意位置, 所以读取数据页是随机的?????

     **IOPS和响应延迟**是随机读的性能. 

     <img src="week4-MySQL%E7%89%A9%E7%90%86%E6%95%B0%E6%8D%AE%E6%A8%A1%E5%9E%8B.assets/image-20200910231451876.png" alt="image-20200910231451876" style="zoom:50%;" />

   



### 34. 生产经验: Linux OS的存储层剖析和IO调度优化原理

Linux的存储系统分层: 

<img src="week4-MySQL%E7%89%A9%E7%90%86%E6%95%B0%E6%8D%AE%E6%A8%A1%E5%9E%8B.assets/3907500_1583145173.jpg" alt="3907500_1583145173" style="zoom:50%;" />

MySQL发起数据页的随机读写, 或者顺序读写, 都会把**IO请求交给VFS层.**

- VFS会把IO请求交给对应目录的文件系统
- 文件系统在Page Cache(OS Cache)缓存里找请求的数据在不在. 在就基于内存缓存来读写. **没有就往下走**
- 通用Block层把 IO请求转换为Block IO请求, 交给IO调度层. (CFQ平等调度算法)
  - 基于公平调度算法, SQL语句读写大量数据的IO操作耗时很久, 进更新少量数据的IO操作会一直等待. 
  - 基于deadline IO调度算法: 任何IO操作不能一直等待, 指定时间范围内必须执行. (调优点)
- IO调度之后, 会决定IO的执行顺序, 发给Block设备驱动层, 
- 经过驱动把请求发给Block设备层(存储硬件): IO操作完成后, 逐层返回. 
- MySQL得到IO读写操作结果



### 35. 生产经验: DB服务器使用的 RAID(存储架构) 介绍

上节是Linux的存储系统原理, 本节讲Linux存储硬件的原理介绍. 是理解MySQL性能抖动的前提. 

> MySQL是一个进程, 跑在Linux操作系统里, Linux负责操作底层硬件

1. 存储硬件

   一般服务器的存储都是搭建的RAID存储架构, **RAID是磁盘冗余矩阵** 用来管理机器的多块磁盘的磁盘矩阵. **读写数据会告诉应该在哪块磁盘上读写.**

   <img src="week4-MySQL%E7%89%A9%E7%90%86%E6%95%B0%E6%8D%AE%E6%A8%A1%E5%9E%8B.assets/image-20200910234943526.png" alt="image-20200910234943526" style="zoom:50%;" />

2. RAID技术的**数据冗余机制**

   RAID磁盘冗余阵列技术把写入的一份数据，在两块磁盘上都写入.

   (具体来说, RAID还可以分成不同的技术方案, 比如RAID 0, RAID 1, RAID 0+1, RAID2)



### 36. 生产经验: RAID存储架构的 电池充放电原理

服务器的多块磁盘组成RAID矩阵, 会有一个RAID卡(带有一个缓存SDRAM).

1. RAID的缓存模式设置成`write back`: 写入磁盘矩阵的数据, 会先缓存在RAID卡缓存里, 后续慢慢写入磁盘. 写缓冲机制. 可以提升写性能. 

2. **为防止断电, RAID卡有独立的锂电池/电容. 锂电池需要配置定时充放电.** 

3. **充放电过程中, RAID的缓存级别变成`write through`, RAID会直接写入磁盘.**

   **(通过缓存可能0.1ms级别, 直接写ms级别)**



### 37. 案例: RAID锂电池充放电导致的MySQL性能抖动 优化

> 数据库是部署在高配置服务器, 6块磁盘配置的RAID10存储
>
> (RAID10指RAID0+RAID1, 每两块磁盘组成一个RAID1互为镜像的架构, 6块有3组RAID1)
>
> 电池30天充放电一次, 数据库写性能下降10倍. 
>
> <img src="week4-MySQL%E7%89%A9%E7%90%86%E6%95%B0%E6%8D%AE%E6%A8%A1%E5%9E%8B.assets/image-20200911001250767.png" alt="image-20200911001250767" style="zoom:33%;" />

**RAID锂电池充放电问题导致的存储性能抖动**

- 把锂电池换成电容.(很麻烦, 容易老化)
- 手动充放电(常用): 关闭自动充放电, 写脚本在低峰触发充放电.
- 充放电时候不关闭write back: 可以和手动充放电一起使用. 





### 38. 案例: 数据库无法连接故障的定位: too many connections



> `ERROR 1040(HY000): Too many connections`. 连接池满了. 没法加socket监听了.
>
> 数据库机器: RAM64G, 高配;
>
> 每个client: 线程池200. 
>
> ![image-20200911002346103](week5-%E7%94%9F%E4%BA%A7%E4%BC%98%E5%8C%96%E6%A1%88%E4%BE%8B.assets/image-20200911002346103.png)

1. 检查MySQL连接池配置:

   `max_connections=800`, 超过了400连接.使用`show variables like 'max_connections'` 查看当前连接数量214个.  

2. 检查MySQL启动日志: 

   > MySQL无法设置max_connections为800, 只能限制到214.
   >
   > 因为: Linux进程可以打开的文件句柄限制1024, 导致最大连接214. (mySQL的源码写死判断)

   ![image-20200911002130286](week5-%E7%94%9F%E4%BA%A7%E4%BC%98%E5%8C%96%E6%A1%88%E4%BE%8B.assets/image-20200911002130286.png)





### 39. 案例: too many connections原理和解决

> 因为linux限制进程的文件句柄, 导致我们没办法创建大量的网络连接, 此时我们的系统进程就没法正常工作了.

1. `ulimit -HSn 65535`  修改最大文件句柄数.  在`/etc/security/limits.conf`, `/etc/rc.local` 两个文件里查看是否被修改. 

2. 修改`my.cnf`文件里的`max_connections` 参数. 
3. 重启MySQL.

4. 原理

   > 通常Linux的句柄数都要调大到66535. 还要调整一些其他的内核参数. 
   >
   > `ulimit`命令可以设置每个进程的资源, -a可以看所有限制. 
   >
   > `/etc/security/limits.conf`这个文件放着所有设置的资源限制. 

