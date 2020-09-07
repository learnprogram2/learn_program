## Week3: Buffer Pool 讲解

DB肯定从插入数据开始. 然后CRUD. 在准备好压测过的, 有完整监控的DB之后开始.

### 11. 从增删改: 看Buffer Pool在数据库中的地位

1. Buffer Pool是什么

   **数据库执行CUD, 主要是对BufferPool里面的真实数据操作的**.  配合了之后的redoLog, 刷磁盘...

   

### 12. Buffer Pool 内存的数据结构

1. 配置Buffer Pool大小

   Buffer Pool是数据库的内存组件, **可以理解成一片内存数据结构**. 默认128MB. 偏小了, 32GB的机器可以分配2GB.  `[server] innodb_buffer_pool_size=2147483648` .

2. **数据页: MySQL中的数据单位**

   数据库的核心数据模型是`表-字段-行`的概念, MySQL的存储把数据抽象**数据页**概念, 存着多行数据. (大小16KB)

   - 如果要更新, DB会找到数据所在的数据页, 从磁盘里把数据页直接加载到Buffer Pool里

     <img src="Untitled.assets/image-20200907225719009.png" alt="image-20200907225719009" style="zoom:50%;" />

3. **磁盘上的数据页 对应到 Buffer Pool的缓存页**

   Buffer Pool中的数据页叫做缓存页, 缓存页和磁盘上的数据页对应, 16KB. 

4. **缓存页的描述信息**

   **每个缓存页, 都有一个描述信息**. 包括: 数据页所属的表空间, 数据页编号, 缓存页在bufferPool中的地址等等.  buffer Pool中设置128, 实际会超出一些(因为描述数据)

   每个缓存页的描述数据放在buffer Pool前面. 大概相当于缓存页的5%(800bytes左右)

   <img src="Untitled.assets/image-20200907230835779.png" alt="image-20200907230835779" style="zoom:50%;" />

5. **思考题:**

   > 如果Buffer pool用尽了, 那么Buffer pool有内存碎片么? 如何减少内存碎片?
   >
   > 答: 因为描述数据里面放着指针, 肯定会有一些缓存失效的成为碎片. 但是缓存页都是16KB, 也很好解决, 尽量分成16kb一块的数据就好了...感觉



### 13. Free链表: 数据页被读取到Buffer Pool

1. 数据库启动时候, **Buffer Pool的初始化**

   启动时候, **会按照Buffer Pool配置的内存大小申请内存区域, 然后按照默认的缓存页16KB, 描述数据800bytes, 在Buffer Pool中划分出一个个缓存页和对应的描述数据空间.**

2. **如何找到空闲的缓存页**

   **DB为Buffer Pool设计出Free双向链表存放空闲的描述数据块的地址**. Buffer Pool初始化之后描述数据块就都会放在Free链表里.

   <img src="Untitled.assets/image-20200907233032491.png" alt="image-20200907233032491" style="zoom:50%;" />

3. Free链表大小

   free链表本身就是buffer pool的描述数据块组成的. 可以认为描述数据块有两个指针, free_pre和free_next. 所以**free链表**不占太多空间(只是**占了一连串小指针**, 还有**基础节点(存放头尾节点地址还有一些基本信息)**).

4. **磁盘中的数据页怎样读到Buffer Pool的缓存页**
   - 从free链表拿一个描述数据块
   - 把数据也放到缓存页里, 补充好描述数据
   - 移除free链表里被占用的描述数据块

5. **如何判断一个数据页有没有对应的缓存页**

   数据库有一个Hash表结构, 表空间号+数据页号作为key, 缓存页的地址作为value. 只要查一下就可以看到有没有缓存

   ![image-20200907235203318](Untitled.assets/image-20200907235203318.png)

6. **思考题**

   > 取一个数据, 必先把把所在的数据页取出来, 数据也属于一个表空间内的. 我们写SQL是表+行, MySQL内部操作是表空间+数据页. 两者的区别和联系是什么?
   >
   > 答: 表和表空间都是指的一张表嘛, 空间更专注存储. 数据页是一个package, 行粒度最小.



### 14. Flush链表: 更新Buffer Pool数据

1. 脏数据页理解

2. 哪些缓存页是脏数据页? flush链表

   引入了flush链表.  类似于free链表, 利用描述数据中两个指针组成双向链表. **所有的修改过的缓存页的描述数据块都会被加入到flush链表中**

   ![image-20200908000517428](Untitled.assets/image-20200908000517428.png)



### 15. LRU算法: Buffer Pool的缓存页不足, 淘汰部分页

























