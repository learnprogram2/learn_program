[How to use Prometheus to monitor JVM](https://www.openlogic.com/blog/prometheus-java-monitoring-and-gathering-data)





1. pod里面的jvm只能看参数, 简单的工具还没有用.
2. 使用grafana监控简单的, 但是JVM的metrics还没有暴露.

3. 怎么配置jvm暴露metrics?
4. 怎么在pipline里面传递JVM 参数? (自己调整) 可以添加, 明天试试.


[输出 GC log](https://blog.gceasy.io/2016/11/15/rotating-gc-log-files/): 可以把GClog输出到其他的地方, 然后按照时间/pid区分.

