## Week 7: 实操环节

### 43. 模拟频繁YoungGC

1. JVM参数规范

   ```shell
   -XX:NewSize=5242880 -XX:MaxNewSize=5242880
   -XX:InitialHeapSize=10485760
   -XX:MaxHeapSize=10485760
   -XX:SurvivorRatio=8
   -XX:PretenureSizeThreshold=20485760
   -XX:UseConcMarkSweepGC
   ```

   <img src="week7.assets/image-20200825193613986.png" alt="image-20200825193613986" style="zoom:67%;" />

2. 打印GC日志

   ```shell
   -XX:+PrintGCDetails # 打印详细GC日志
   -XX:+PrintGCTimeStamps # 打印每次GC时间
   -Xloggc:.../%t-%p_gc.log # GC日志写入文件.
   ```



### 44. JVM youngGC日志

1. 文件开头的JVM参数

2. 一行GC记录.

   ```log
   0.472: [GC (Allocation Failure) 0.472: [ParNew: 3721K->282K(4608K), 0.0011412 secs] 4502K->1575K(9728K), 0.0011827 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
   ```

   时间戳: GC原因 : 新生代GC收集器ParNew, 耗时. 

   

   

3. 最后是GC过后的堆内存情况

   ```shell
   Heap
    # 新生代的占用情况
    par new generation   total 4608K, used 1508K [0x00000000ff600000, 0x00000000ffb00000, 0x00000000ffb00000)
     eden space 4096K,  29% used [0x00000000ff600000, 0x00000000ff732768, 0x00000000ffa00000)
     from space 512K,  55% used [0x00000000ffa00000, 0x00000000ffa46950, 0x00000000ffa80000)
     to   space 512K,   0% used [0x00000000ffa80000, 0x00000000ffa80000, 0x00000000ffb00000)
    # CMS管理的老年代内存5mb, 用了1m多
    concurrent mark-sweep generation total 5120K, used 1292K [0x00000000ffb00000, 0x0000000100000000, 0x0000000100000000)
    # 元数据空间和class空间.
    Metaspace       used 3218K, capacity 4496K, committed 4864K, reserved 1056768K
     class space    used 352K, capacity 388K, committed 512K, reserved 1048576K
   ```

4. **思考题:** 

   > 对最后的Metaspace, JDK1.8之后的Metaspace和Classspace里面放的什么内容? 然后used, capacity, committeed, reserved都是什么意思?
   >
   > used: 使用的, capacity: 当前分配块的元数据空间; committed: 空间快的数量; reserved: 元数据的空间保留.



### 45. 模拟对象进入老年代

```java
1. 躲过n次GC, 达到年龄阈值了之后
2. 动态年龄判断, Survivor区内年龄[1,2,3...]的对象大于50%了之后, n以上的对象进入老年代.
3. YoungGC后Survivor放不下了, 都进入老年代 (这个是不对的, 部分对象会放在survivor区里的)
4. 大对象进入老年代.
```

1. 动态年龄判断规则: 

   ```shell
   # jvm参数设置10MB的新生代和10MB的老年代.
   -XX:NewSize=10M
   -XX:MaxNewSize=10M
   -XX:InitialHeapSize=20M
   -XX:MaxHeapSize=20M
   -XX:SurvivorRatio=8
   -XX:PretenureSizeThreshold=20485760
   -XX:+UseConcMarkSweepGC
   -XX:+PrintGCDetails
   -XX:+PrintGCTimeStamps
   ```

2. 思考题: 

   > 模拟处对象达到15岁后自然进入老年代;
   >
   > for循环里不断地new对象, 然后for之前拿着一个对象. 这个对象就不断地长年龄吧.

3. 思考题2:

   > 分配一个大对象, 让他直接进入老年代, 看GC日志是否会直接进入老年代?
   >
   > 是的, 没有GC日志, 直接就放在老年代了.



### 47. 查看JVM的FullGC日志

```shell
0.506: [GC (CMS Initial Mark) [1 CMS-initial-mark: 11496K(16384K)] 11496K(25600K), 0.0002282 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
0.506: [CMS-concurrent-mark-start]
0.508: [CMS-concurrent-mark: 0.002/0.002 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
0.508: [CMS-concurrent-preclean-start]
0.508: [CMS-concurrent-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
# 上面是简单的CMS gc
```

/// 这里做了一个模拟老年代GC的案例. 自己在电脑上模拟不太出来...



### 48. 作业

> 分析线上JVM gc日志:
>
> 分析过了.问题就是刚开始为什么有三次fullGC, 老年代增加, 元数据区却不变.
>
> 



### 49. 问题:

1. 动态年龄判断, 年龄1-5的对象占有S区的50%, 那么`>=`5的对象会进入老年代
2. 动态年龄判断, 每次MinorGC, 都尽量保证存活对象`<=`s区的50%.
3. 动态年龄判断实在每次MinorGC之后运行的





