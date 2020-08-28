## Week8: 

### 50. 实验: `jstat`摸清JVM运行状况

1. `jstat -gc PID` 看到JVM的内存和GC情况
2. 










### 51. Jmap和Jhat搭配监控对象分布



3. **使用jmap生成堆内存转储快照**

   `jmap -dump:live,format=b,file=dump.hprof PID` 会生成一个二进制的dump文件.

4. 使用Jhat查看dump文件

   jhat可以分析Heap的Dump. 内置了web浏览器, 可以放在浏览器看

   `jhat dump.hprof -port 8080`

5. 思考题: 

   >  使用一下jmap搭配jhat查看Heap快照. 





### 52. 从测试到上线, 如何分析JVM运行合理优化

结合上面介绍的 `jstat`和`jmap+jhat`, 做实际开发的JVM优化梳理

1. 开发好系统之后的`预估性优化`

   
























