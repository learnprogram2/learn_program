[How to use Prometheus to monitor JVM](https://www.openlogic.com/blog/prometheus-java-monitoring-and-gathering-data)





1. pod里面的jvm只能看参数, 简单的工具还没有用.
2. 使用grafana监控简单的, 但是JVM的metrics还没有暴露.

3. 怎么配置jvm暴露metrics?
4. 怎么在pipline里面传递JVM 参数? (自己调整) 可以添加, 明天试试.


[输出 GC log](https://blog.gceasy.io/2016/11/15/rotating-gc-log-files/): 可以把GClog输出到其他的地方, 然后按照时间/pid区分.

```shell
bash-4.2$ java -XX:+PrintGCDetails GcCollectorPrinter
Error: Could not find or load main class GcCollectorPrinter
Heap
 def new generation   total 19648K, used 1056K [0x00000000c0000000, 0x00000000c1550000, 0x00000000d5550000)
  eden space 17472K,   6% used [0x00000000c0000000, 0x00000000c0108178, 0x00000000c1110000)
  from space 2176K,   0% used [0x00000000c1110000, 0x00000000c1110000, 0x00000000c1330000)
  to   space 2176K,   0% used [0x00000000c1330000, 0x00000000c1330000, 0x00000000c1550000)
 tenured generation   total 43712K, used 0K [0x00000000d5550000, 0x00000000d8000000, 0x0000000100000000)
   the space 43712K,   0% used [0x00000000d5550000, 0x00000000d5550000, 0x00000000d5550200, 0x00000000d8000000)
 Metaspace       used 2529K, capacity 4480K, committed 4480K, reserved 1056768K
  class space    used 282K, capacity 384K, committed 384K, reserved 1048576K
 ```