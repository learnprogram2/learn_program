## Week7: 事务

### 47. MySQL多个事务并发场景

![image-20200913133018679](week7-%E4%BA%8B%E5%8A%A1%E4%BB%8B%E7%BB%8D.assets/image-20200913133018679.png)

多事务并发执行问题:

1. 更新冲突, 加锁?
2. 读写冲突怎么办?

> **解决多个事务并发冲突: 事务隔离级别, MVCC多版本隔离, 锁机制...**



### 48. 读写冲突: 脏读和脏写

读写冲突会发生: 脏写, 脏读, 不可重复读, 幻读

1. 脏写: 

   事务A先更新为x1, 事务B更新为x2. **事务A写的丢掉了, 就是脏写.**

2. 脏读:

   事务A读出x1, 事务B更新为x2, **事务A读到的丢失了, 就是脏读**

3. **脏读脏写, 都是事务内的数据收到其它事务的干扰, 变成了脏数据.**



### 49. 不可重复读

> 事务里, 多次读一条数据不一致, 就是不可重复读么?  是的~
>
> 侧重于数据的修改



### 50. 幻读

> 事务A搜索一定范围内的数据, 多次查询发现数目增加. 就是幻读.
>
> 侧重于数据的新增.



### 51. SQL标准的事务4个隔离级别

> Read Uncommitted, Read Committed, Repeatable Read, Serializable. to **solve the concurrence problems**
>
> 读未提交, 读提交, 可重复读, 串行化.

1. read uncommitted: 不允许脏写发生. 不让两个事务修改一行数据. 
2. read committed: 不允许脏写, 脏读. 事务内没有提交的数据其它事务读不到.
3. Repeatable Read: 可重复读, **可能会幻读**. 事务里不会读到数据的修改.

4. Serializable: 串行执行事务. 





### 52. MySQL事务隔离级别支持, Spring事务注解 使用

**MySQL默认是RR级别**.  但是MySQL的RR语义与SQL标准的不太相同, MySQL的RR可以防止幻读.

依靠的是Multi-Version Control实现的. 

**修改MySQL事务隔离级别**: set [GLOBAL|SESSION] Transaction ISOLATION LEVEL ${level}. 

Spring的@Transaction注解就是标注了Session内的隔离级别. 























