rt

触发一次OOM一次, 没有数据进来, 但是内存很高.

GC每次回收一点, 下降点也很高.


第一次savepoint, 之后的checkpoint很大. 



- 检查代码, 内存分配情况.
- 并行度开到10, 内存8G. 看一下速度.




4. autosys not work
5. request丢掉了100w



执行savepoint JVM restart 
==================================================================================

## 这个pod host="10_57_21_223", instance="10.57.21.223:9249", kubernetes_pod_name="gspdata-subscription-smc-json-mapping-uat-taskmanager-10-rzt4l", 
##	tm_id="32ae85b1c67bd5687f6a69b2d514f76b", venv="uat"}217.9 MB
	
{"thread":"Checkpoint Timer","level":"INFO","loggerName":"org.apache.flink.runtime.checkpoint.CheckpointCoordinator","message":"Triggering checkpoint 564 (type=SAVEPOINT) @ 1606445753406 for job 00000000000000000000000000000000.","endOfBatch":false,"loggerFqcn":"org.apache.logging.slf4j.Log4jLogger","instant":{"epochSecond":1606445753,"nanoOfSecond":417000000},"threadId":120,"threadPriority":5,"@timestamp":"2020-11-27_02:55:53.417"}

executiongraph.ExecutionGraph","message":"Source: DATA_SOURCE -> SOURCE_RULE_PROCESS (6/10) (5f420dfe17f87eeda0bf1503a57431ed) 
	switched from RUNNING to FAILED on org.apache.flink.runtime.jobmaster.slotpool.SingleLogicalSlot@2a41ec6a.",
	"thrown":{"commonElementCount":0,"localizedMessage":"Job leader for job id 00000000000000000000000000000000 lost leadership.",
	"message":"Job leader for job id 00000000000000000000000000000000 lost leadership.",
	"name":"java.lang.Exception",
	"extendedStackTrace":"java.lang.Exception: Job leader for job id 00000000000000000000000000000000 lost leadership.
		\tat org.apache.flink.runtime.taskexecutor.TaskExecutor$JobLeaderListenerImpl.lambda$null$2(TaskExecutor.java:1852) ~[a-a-apache-flink-1.11.2-patch.jar:?]
		\tat java.util.Optional.ifPresent(Optional.java:159) ~[?:1.8.0_241]
		\tat org.apache.flink.runtime.taskexecutor.TaskExecutor$JobLeaderListenerImpl.lambda$jobManagerLostLeadership$3(TaskExecutor.java:1851) ~[a-a-apache-flink-1.11.2-patch.jar:?]
		\tat org.apache.flink.runtime.rpc.akka.AkkaRpcActor.handleRunAsync(AkkaRpcActor.java:402) ~[a-a-apache-flink-1.11.2-patch.jar:?]
		\tat org.apache.flink.runtime.rpc.akka.AkkaRpcActor.handleRpcMessage(AkkaRpcActor.java:195) ~[a-a-apache-flink-1.11.2-patch.jar:?]
		\tat org.apache.flink.runtime.rpc.akka.AkkaRpcActor.handleMessage(AkkaRpcActor.java:152) ~[a-a-apache-flink-1.11.2-patch.jar:?]
		\tat akka.japi.pf.UnitCaseStatement.apply(CaseStatements.scala:26) [a-a-apache-flink-1.11.2-patch.jar:?]
		\tat akka.japi.pf.UnitCaseStatement.apply(CaseStatements.scala:21) [a-a-apache-flink-1.11.2-patch.jar:?]
		\tat scala.PartialFunction.applyOrElse(PartialFunction.scala:123) [a-a-apache-flink-1.11.2-patch.jar:?]
		\tat scala.PartialFunction.applyOrElse$(PartialFunction.scala:122) [a-a-apache-flink-1.11.2-patch.jar:?]
		\tat akka.japi.pf.UnitCaseStatement.applyOrElse(CaseStatements.scala:21) [a-a-apache-flink-1.11.2-patch.jar:?]
		\tat scala.PartialFunction$OrElse.applyOrElse(PartialFunction.scala:171) [a-a-apache-flink-1.11.2-patch.jar:?]
		\tat scala.PartialFunction$OrElse.applyOrElse(PartialFunction.scala:172) [a-a-apache-flink-1.11.2-patch.jar:?]
		\tat scala.PartialFunction$OrElse.applyOrElse(PartialFunction.scala:172) [a-a-apache-flink-1.11.2-patch.jar:?]
		\tat akka.actor.Actor.aroundReceive(Actor.scala:517) [a-a-apache-flink-1.11.2-patch.jar:?]
		\tat akka.actor.Actor.aroundReceive$(Actor.scala:515) [a-a-apache-flink-1.11.2-patch.jar:?]
		\tat akka.actor.AbstractActor.aroundReceive(AbstractActor.scala:225) [a-a-apache-flink-1.11.2-patch.jar:?]
		\tat akka.actor.ActorCell.receiveMessage(ActorCell.scala:592) [a-a-apache-flink-1.11.2-patch.jar:?]
		\tat akka.actor.ActorCell.invoke(ActorCell.scala:561) [a-a-apache-flink-1.11.2-patch.jar:?]
		\tat akka.dispatch.Mailbox.processMailbox(Mailbox.scala:258) [a-a-apache-flink-1.11.2-patch.jar:?]
		\tat akka.dispatch.Mailbox.run(Mailbox.scala:225) [a-a-apache-flink-1.11.2-patch.jar:?]
		\tat akka.dispatch.Mailbox.exec(Mailbox.scala:235) [a-a-apache-flink-1.11.2-patch.jar:?]
		\tat akka.dispatch.forkjoin.ForkJoinTask.doExec(ForkJoinTask.java:260) [a-a-apache-flink-1.11.2-patch.jar:?]
		\tat akka.dispatch.forkjoin.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1339) [a-a-apache-flink-1.11.2-patch.jar:?]
		\tat akka.dispatch.forkjoin.ForkJoinPool.runWorker(ForkJoinPool.java:1979) [a-a-apache-flink-1.11.2-patch.jar:?]
		\tat akka.dispatch.forkjoin.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:107) [a-a-apache-flink-1.11.2-patch.jar:?]
		"},"endOfBatch":false,"loggerFqcn":"org.apache.logging.slf4j.Log4jLogger",
	"instant":{"epochSecond":1606445832,"nanoOfSecond":997000000},"threadId":9917,"threadPriority":5,"@timestamp":"2020-11-27_02:57:12.997"}
executiongraph.failover.flip1.RestartPipelinedRegionFailoverStrategy","message":
	"Calculating tasks to restart to recover the failed task e85726ba944c3a2188b5ca4a1e5d31f3_5.",
	"endOfBatch":false,"loggerFqcn":"org.apache.logging.slf4j.Log4jLogger",
	"instant":{"epochSecond":1606445833,"nanoOfSecond":51000000},
	"threadId":9917,"threadPriority":5,"@timestamp":"2020-11-27_02:57:13.051"}
executiongraph.failover.flip1.RestartPipelinedRegionFailoverStrategy",
	"message":"20 tasks should be restarted to recover the failed task e85726ba944c3a2188b5ca4a1e5d31f3_5. ",
	"endOfBatch":false,"loggerFqcn":"org.apache.logging.slf4j.Log4jLogger",
	
executiongraph.ExecutionGraph","message":"Job gspdata-subscription-smc-mapping (00000000000000000000000000000000) switched from state RUNNING to RESTARTING.",
	"endOfBatch":false,"loggerFqcn":"org.apache.logging.slf4j.Log4jLogger","instant":{"epochSecond":1606445833,"nanoOfSecond":55000000},"threadId":9917,"threadPriority":5,"@timestamp":"2020-11-27_02:57:13.055"}
executiongraph.ExecutionGraph","message":"INDEX_MESSAGE_KEYED_PROCESSOR -> SECURITY_MAPPING_PROCESSOR -> Sink: SECURITY_MAPPING_SINK (2/10) (081c865f87e83f7fe1149ba5ecf9c521) 
	switched from RUNNING to CANCELING.","endOfBatch":false,"loggerFqcn":"org.apache.logging.slf4j.Log4jLogger","instant":{"epochSecond":1606445833,"nanoOfSecond":59000000},"threadId":9917,"threadPriority":5,"@timestamp":"2020-11-27_02:57:13.059"}
executiongraph.ExecutionGraph" ......................






======================================

{"thread":"flink-akka.actor.default-dispatcher-9923","level":"INFO","loggerName":"org.apache.flink.runtime.executiongraph.ExecutionGraph","message":"INDEX_MESSAGE_KEYED_PROCESSOR -> SECURITY_MAPPING_PROCESSOR -> Sink: SECURITY_MAPPING_SINK (2/10) (f682d65339f1ae8ed4d3cf8732b0300e) switched from RUNNING to FAILED on org.apache.flink.runtime.jobmaster.slotpool.SingleLogicalSlot@6929f626.","thrown":{"commonElementCount":0,"localizedMessage":"Connection unexpectedly closed by remote task manager '10.57.3.239/10.57.3.239:41885'. This might indicate that the remote task manager was lost.","message":"Connection unexpectedly closed by remote task manager '10.57.3.239/10.57.3.239:41885'. This might indicate that the remote task manager was lost.","name":"org.apache.flink.runtime.io.network.netty.exception.RemoteTransportException","extendedStackTrace":"org.apache.flink.runtime.io.network.netty.exception.RemoteTransportException: Connection unexpectedly closed by remote task manager '10.57.3.239/10.57.3.239:41885'. This might indicate that the remote task manager was lost.\n\tat org.apache.flink.runtime.io.network.netty.CreditBasedPartitionRequestClientHandler.channelInactive(CreditBasedPartitionRequestClientHandler.java:144) ~[a-a-apache-flink-1.11.2-patch.jar:?]\n\tat org.apache.flink.shaded.netty4.io.netty.channel.AbstractChannelHandlerContext.invokeChannelInactive(AbstractChannelHandlerContext.java:257) ~[a-a-apache-flink-1.11.2-patch.jar:?]\n\tat org.apache.flink.shaded.netty4.io.netty.channel.AbstractChannelHandlerContext.invokeChannelInactive(AbstractChannelHandlerContext.java:243) ~[a-a-apache-flink-1.11.2-patch.jar:?]\n\tat org.apache.flink.shaded.netty4.io.netty.channel.AbstractChannelHandlerContext.fireChannelInactive(AbstractChannelHandlerContext.java:236) ~[a-a-apache-flink-1.11.2-patch.jar:?]\n\tat org.apache.flink.shaded.netty4.io.netty.channel.ChannelInboundHandlerAdapter.channelInactive(ChannelInboundHandlerAdapter.java:81) ~[a-a-apache-flink-1.11.2-patch.jar:?]\n\tat org.apache.flink.runtime.io.network.netty.NettyMessageClientDecoderDelegate.channelInactive(NettyMessageClientDecoderDelegate.java:97) ~[a-a-apache-flink-1.11.2-patch.jar:?]\n\tat org.apache.flink.shaded.netty4.io.netty.channel.AbstractChannelHandlerContext.invokeChannelInactive(AbstractChannelHandlerContext.java:257) ~[a-a-apache-flink-1.11.2-patch.jar:?]\n\tat org.apache.flink.shaded.netty4.io.netty.channel.AbstractChannelHandlerContext.invokeChannelInactive(AbstractChannelHandlerContext.java:243) ~[a-a-apache-flink-1.11.2-patch.jar:?]\n\tat org.apache.flink.shaded.netty4.io.netty.channel.AbstractChannelHandlerContext.fireChannelInactive(AbstractChannelHandlerContext.java:236) ~[a-a-apache-flink-1.11.2-patch.jar:?]\n\tat org.apache.flink.shaded.netty4.io.netty.channel.DefaultChannelPipeline$HeadContext.channelInactive(DefaultChannelPipeline.java:1416) ~[a-a-apache-flink-1.11.2-patch.jar:?]\n\tat org.apache.flink.shaded.netty4.io.netty.channel.AbstractChannelHandlerContext.invokeChannelInactive(AbstractChannelHandlerContext.java:257) ~[a-a-apache-flink-1.11.2-patch.jar:?]\n\tat org.apache.flink.shaded.netty4.io.netty.channel.AbstractChannelHandlerContext.invokeChannelInactive(AbstractChannelHandlerContext.java:243) ~[a-a-apache-flink-1.11.2-patch.jar:?]\n\tat org.apache.flink.shaded.netty4.io.netty.channel.DefaultChannelPipeline.fireChannelInactive(DefaultChannelPipeline.java:912) ~[a-a-apache-flink-1.11.2-patch.jar:?]\n\tat org.apache.flink.shaded.netty4.io.netty.channel.AbstractChannel$AbstractUnsafe$8.run(AbstractChannel.java:816) ~[a-a-apache-flink-1.11.2-patch.jar:?]\n\tat org.apache.flink.shaded.netty4.io.netty.util.concurrent.AbstractEventExecutor.safeExecute(AbstractEventExecutor.java:163) ~[a-a-apache-flink-1.11.2-patch.jar:?]\n\tat org.apache.flink.shaded.netty4.io.netty.util.concurrent.SingleThreadEventExecutor.runAllTasks(SingleThreadEventExecutor.java:416) ~[a-a-apache-flink-1.11.2-patch.jar:?]\n\tat org.apache.flink.shaded.netty4.io.netty.channel.epoll.EpollEventLoop.run(EpollEventLoop.java:331) ~[a-a-apache-flink-1.11.2-patch.jar:?]\n\tat org.apache.flink.shaded.netty4.io.netty.util.concurrent.SingleThreadEventExecutor$5.run(SingleThreadEventExecutor.java:918) ~[a-a-apache-flink-1.11.2-patch.jar:?]\n\tat org.apache.flink.shaded.netty4.io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) ~[a-a-apache-flink-1.11.2-patch.jar:?]\n\tat java.lang.Thread.run(Thread.java:748) ~[?:1.8.0_241]\n"},"endOfBatch":false,"loggerFqcn":"org.apache.logging.slf4j.Log4jLogger","instant":{"epochSecond":1606447892,"nanoOfSecond":221000000},"threadId":10487,"threadPriority":5,"@timestamp":"2020-11-27_03:31:32.221"}
{"thread":"flink-akka.actor.default-dispatcher-9923","level":"INFO","loggerName":"org.apache.flink.runtime.executiongraph.failover.flip1.RestartPipelinedRegionFailoverStrategy","message":"Calculating tasks to restart to recover the failed task feb3a6f0257a8f3304c151b98b06d3e1_1.","endOfBatch":false,"loggerFqcn":"org.apache.logging.slf4j.Log4jLogger","instant":{"epochSecond":1606447892,"nanoOfSecond":231000000},"threadId":10487,"threadPriority":5,"@timestamp":"2020-11-27_03:31:32.231"}



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







# 而且 系统跑完了之后, 被压不大了, 内存占用还是很大. 怀疑OOM