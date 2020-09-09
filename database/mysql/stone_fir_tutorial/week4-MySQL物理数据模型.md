## MySQL物理数据模型 

上一篇讲了, MySQL是如何把数据页加载到缓存页里的. 本节讲undoLog和redoLog和事务机制之前的数据缓存机制和内存数据更新机制.

### 24. MySQL如何在磁盘里存储一行数据

表,行,字段是逻辑概念, **表空间, 数据区和数据页是物理存储概念.** 

1. MySQL为什么引入数据页概念.

   组成一个块, 让更新效率更高.

2. **一行数据在磁盘上是如何存储的**

   行格式: 行存储的格式. 可以指定. `create table t1(columns..) ROW_FORMAT=COMPACT`. 

   COMPACT格式: 每行数据存储格式: [变长字段的长度列表, null值列表, 数据头, column1的值, column2的值...]

   \\\

### 25. VARCHAR变长字段的存储

本小节讲每行数据的额外信息里放的什么

1. **变长字段在磁盘中怎么存储的**

   大坨数据堆在一起存储: Varchar(10), char(1), char(1) : hello a b

2. **变长字段的长度列表, 解决一行数据的读取**

   每行数据加上附加信息:  "0x05 null值列表 数据头 hello a b"

3. 多个变长字段的长度列表: 

   逆序的. 如果一行是varchar(10) varchar(5) varchar(20) ,,,. 它的**变长字段长度列表是逆序的**

4. 思考题:

   > 为什么要把每行数据紧挨在一起存放. 如果存成对象反序列化会不会更好?
   >
   > 答: 主要是节省空间吧. 



### 26. 每行数据的多个null字段如何存储

1. **null不能直接存储, Null值用bit位存储: 不通过字符串存储,** 

3. **栗子:** 

   table的列: c1 varchar(10) not null, c2 varchar(20), c3 char(1), c4 varchar(30), c5 varchar(50).

   如果c2-c5都是null.  用8个起步的byte位(8n) 不足8个的高位补0. null的column就是1

   ```text
   # 磁盘存储格式应该是
   变长字段长度列表  null值列表 头信息 column1=value1 column2=value2 column3=value3 column4=value4 column5=value5
   # 如果变长字段是null, 就不用在[变长字段长度列表]里存放长度了. 只有c1是not null的
   [0x05] [000 01111] [头信息] [column1=value1 ...]
   ```

   

4. 读取一行数据:

   知道了哪一位为null, 也知道了varchar的字段长度, 就可以顺序的从数据页里面读取一行数据了.

5. 思考题:

   > 直接使用NULL字符串存储, 和使用bit位运算存储, 有多爱好存储空间的差距呢?
   >
   > 答: null字符串, 每个null要占用2字节. 使用bit两个字节可以标记16个column的表.  差距太大.



### 27. 数据头40个bit位(5字节)















