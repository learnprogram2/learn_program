## 案例

### 109-111: 运营系统 SQL调优: (避免全扫all)

运营系统常见功能: 筛选出部分用户, 做活动. 一张表里存储用户账号信息users, 另一张表里存储用户的标签users_extent_info(爱好, 地域...).

> `select id, name from users where id in (select user_id from users_extent_info where latest_login_time < xxxx)`
>
> 筛选出登录时间之前的用户.
>
> 一般运行这种IN大集合的SQL, 先跑一个count看看有多少条.  如果表太大, 可以分页读出来. 
>
> **性能瓶颈:** 运行时间长达数十秒.
>
> ![image-20200920123341310](week11-%E6%A1%88%E4%BE%8B.assets/image-20200920123341310.png)
>
> **执行计划:**
>
> - 先从userInfor里面range 查index, materialized物化出一张表<subquery2>.
> - 全表扫users表, 循环查, 遍历查找临时表. 4561*49651次查找.
>
> **问题:** 为什么和IN里面的集合物化出来的表做连表, 不用user里面的索引, 物化表也没有索引?
>
> 拿到执行计划之后, 可以用`show warnings` 命令, 看到会把IN给修改成`semi join`; 这个会遍历所有的左表, 然后在右表有就返回. 
>
> **解决:** 
>
> - 执行`set optimizer_switch='semijoin=off'`, 关掉半连接优化, 会子查询查到4561个数据, 然后基于主键查主表. 不会全表扫. 
> - 修改SQL语法: 把执行计划的semi-join优化避免掉.
>
> **结论: 一定要用索引, 避免ALL**







### 112-114. 商品系统 SQL调优(强制使用索引, 避免优化器选择all)

情况: 商品系统报警出现慢SQL. 然后连接池打满. SQL突然执行几十秒.

SQL: `select * from products where category='xx' and sub_category='xxx' order by id desc limit 0,10`

**表配置:** products表, 1亿条数据, key(category, sub_category)

**问题:** 执行计划的possible_keys里有我们的key, 但是执行的key用的是primary, 扫了一遍primary-Key, 相当于ALL了. 

**解决:** 禁止使用聚簇索引扫描. **force index()**

`select * from products FORCE index(index_category) where category='x' and sub category='xx' order by id desc limit 0,10` 

**原因:** 

- 为什么要用聚簇索引扫描:

  亿级大表, 二级索引(index_category)也很大, 可能扫出10w个id, 进行filesort磁盘排序, , 还要回表, 如果扫描聚簇索引只扫出来10个, 所以优化的时候就选择了主键.

- 为什么之前扫聚簇索引不慢?

  因为之前`where category='xx' and sub_category='xx'`条件是有商品的, 分页拿前10条, 一般扫一点就出来了, 后来新增了一些没有商品的category, 然后使用where扫不出来数据, 全表扫描了. 







### 115. 评论系统 SQL优化(减少没用的回表, 直接All)

**情况:** 商品评论系统, 10亿+的数据量, 分库分表处理, 单表百万级别. 热门商品评论几十万条, 然后用户分页查询, 出现了**深度分页问题**

**SQL:** `select * from commets where product_id = 'xx' and is_good_comment='5' order by id desc limit 100000,20`查看5分好评的深分页. 

**问题:** 有索引key(product_id), 但索引里没有is_good_comment的评分, 所以要回表. 也就是查出来的几十W评论都是不确定数据, 都要回表. 

**优化:** select * from comments a, (select id from comments where product_id=xx and is_good_comment=5 order by id desc limit 100000,20) b where a.id = b.id

使用了子查询, 先进子表里扫描聚簇索引把id查出来(感觉应该是range), 构成20条数据的临时表, 然后按照id去查完整数据就好了.

> is_good_comment 字段经常变化, 不适合建立索引. 每次都要调整好就. 



### 116-117. 使用profiling排查千万级数据删除导致的慢查询

**情况:** 对同一个表的单行查询的慢SQL出现, 排除了MySQL服务器整体负载过高. 如果所有SQL不加区分的慢, 那么应该去排除CPU/网络IO/磁盘IO负载.

**通常排查分两头:** 通过执行计划检查SQL是否有问题, 检查MySQL服务器负载

**第三种排查: 使用MySQL profilling分析SQL执行过程和耗时**

1. Sending-Data耗时高

```java
// 1. 在MySQL上开启profiling参数
root@localhost : (none) 10:53:11> set profiling=1;
// 2. 执行SQL
// 3. show profiles 命令展示sql执行情况. 看各种耗时.
// 4. 发现 Sending-Data耗时最高
```

2. `show engine innodb status` 发现`history list length`指标过高, 这说明有大量事务并发执行, undo多版本快照链条很长.

**最终原因:** 发现定时任务在跑: 开启事务, 事务内删除上千万数据. 导致其他事物查询的时候按照read view判定原则扫描到已经删除, 会在历史版本链中找到自己能看到的版本. 

[文章链接🔗](https://apppukyptrl1086.pc.xiaoe-tech.com/detail/i_5f5eb2aae4b0d59c87b5940c/1?from=p_5e0c2a35dbbc9_MNDGDYba&type=6)

**同学案例:** 有个错误消息日志表, 接收MQ消费失败的数据, 后台定时重试. 数据库总是晚上就每隔一小时出现一个链接异常(数据库分片分库的，dbproxy与底层库出现链接异常), 时间集中, 所以是定时任务的问题. 原因: 表内数据量达到了百万级, 定时任务一个大事务, 更新几十万条消息, 每条消息需要调用第三方服务10s, 所以事务更长. 分页语句也有问题, 每次都要回表.导致MySQL压力太大与dbproxy断开. 解决: 事务缩小到每一条, 分页查询不要回表.



















