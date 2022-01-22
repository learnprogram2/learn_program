request太多, 阻塞其他数据的处理
解决: 查日志, 抓出client, disable掉这个client
问题: 不应该动生产数据库
解决: request项目应该承受request query, HBase接收.

现在不用HTTP接收request query的原因:
Kafka MQ稳定高并发: 600QPS/s, 未来要拆到ES/Hbase.




