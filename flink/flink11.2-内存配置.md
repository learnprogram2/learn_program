
Exception in thread "main" org.apache.flink.configuration.IllegalConfigurationException: 
The configured Total Process Memory size (3.000gb (3221225472 bytes)) is less than the sum of the derived Total Flink Memory size (4.000gb (4294967296 bytes)) 
and the configured or default JVM Metaspace size  (256.000mb (268435456 bytes)).



# jobmanager 配置: 
Total Flink memory		jobmanager.memory.flink.size	0.75G
Total process memory	jobmanager.memory.process.size	1.25G
# jobmanager 日志里面自动分配更细微的
2020-10-20 05:48:02,052 [main] INFO  org.apache.flink.configuration.GlobalConfiguration - Loading configuration property: jobmanager.rpc.address, 10.57.19.47
2020-10-20 05:48:02,146 [main] INFO  org.apache.flink.runtime.util.bash.BashJavaUtils - Final Master Memory configuration:
2020-10-20 05:48:02,147 [main] INFO  org.apache.flink.runtime.util.bash.BashJavaUtils -   Total Process Memory: 2.000gb (2147483648 bytes)
2020-10-20 05:48:02,148 [main] INFO  org.apache.flink.runtime.util.bash.BashJavaUtils -     Total Flink Memory: 1.250gb (1342177280 bytes)
2020-10-20 05:48:02,148 [main] INFO  org.apache.flink.runtime.util.bash.BashJavaUtils -       JVM Heap:         1.125gb (1207959552 bytes)
2020-10-20 05:48:02,149 [main] INFO  org.apache.flink.runtime.util.bash.BashJavaUtils -       Off-heap:         128.000mb (134217728 bytes)
2020-10-20 05:48:02,150 [main] INFO  org.apache.flink.runtime.util.bash.BashJavaUtils -     JVM Metaspace:      256.000mb (268435456 bytes)
2020-10-20 05:48:02,150 [main] INFO  org.apache.flink.runtime.util.bash.BashJavaUtils -     JVM Overhead:       512.000mb (536870912 bytes)
## 4Gb 分配的
{"logEvent":"  Total Process Memory:          4.000gb (4294967296 bytes)","@timestamp":"2020-11-27_03:35:39.458"}
{"logEvent":"    Total Flink Memory:          3.000gb (3221225472 bytes)","@timestamp":"2020-11-27_03:35:39.459"}
{"logEvent":"      Total JVM Heap Memory:     1.375gb (1476394984 bytes)","@timestamp":"2020-11-27_03:35:39.459"}
{"logEvent":"        Framework:               128.000mb (134217728 bytes)","@timestamp":"2020-11-27_03:35:39.460"}
{"logEvent":"        Task:                    1.250gb (1342177256 bytes)","@timestamp":"2020-11-27_03:35:39.460"}
{"logEvent":"      Total Off-heap Memory:     1.625gb (1744830488 bytes)","@timestamp":"2020-11-27_03:35:39.461"}
{"logEvent":"        Managed:                 1.200gb (1288490208 bytes)","@timestamp":"2020-11-27_03:35:39.461"}
{"logEvent":"        Total JVM Direct Memory: 435.200mb (456340280 bytes)","@timestamp":"2020-11-27_03:35:39.462"}
{"logEvent":"          Framework:             128.000mb (134217728 bytes)","@timestamp":"2020-11-27_03:35:39.462"}
{"logEvent":"          Task:                  0 bytes","@timestamp":"2020-11-27_03:35:39.463"}
{"logEvent":"          Network:               307.200mb (322122552 bytes)","@timestamp":"2020-11-27_03:35:39.464"}
{"logEvent":"    JVM Metaspace:               256.000mb (268435456 bytes)","@timestamp":"2020-11-27_03:35:39.464"}
{"logEvent":"    JVM Overhead:                768.000mb (805306368 bytes)","@timestamp":"2020-11-27_03:35:39.465"}





# taskmanager 配置