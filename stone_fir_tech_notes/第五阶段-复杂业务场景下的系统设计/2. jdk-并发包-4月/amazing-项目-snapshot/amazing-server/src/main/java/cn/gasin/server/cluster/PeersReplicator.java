package cn.gasin.server.cluster;

import cn.gasin.api.http.BaseClientRequest;
import cn.gasin.api.http.heartbeat.HeartbeatRequest;
import cn.gasin.api.http.register.RegisterRequest;
import cn.gasin.api.server.cluster.PeersReplicateBatch;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * 负责register-server集群之间的同步
 * <p>
 * 同步client的 CRUD: 注册, 心跳, 下线 三个功能
 * <p>
 * ConcurrentLinkedQueue, 无界队列, 纯CAS实现的无锁机制,多线程并发的性能很高.
 * LinkedBlockingQueue, 有界队列, 基于两把独占锁实现的, 并发稍低, 但是可以实现指定大小的有界的限制,避免内存无限制的膨胀.
 */
@Log4j2
@Component
public class PeersReplicator {
    // 一个batch的最大数量
    public static final Integer PEER_REPLICATOR_BATCH_QUEUE_SIZE = 10000;
    // 生成batch的间隔
    public static final Integer PEER_REPLICATOR_BATCH_INTERVAL = 500;

    public static final Integer PEER_REPLICATOR_REPLICATE_QUEUE_SIZE = 10000;
    /**
     * 第一层队列: 接受高并发写入. 无界队列
     * 使用无限队列比有限队列要好一点, 不用丢数据或者阻塞.
     */
    private final ConcurrentLinkedQueue<BaseClientRequest> acceptorQueue = new ConcurrentLinkedQueue<>();
    /**
     * 第二层队列: batch生成
     */
    private final LinkedBlockingQueue<BaseClientRequest> currentBatchQueue = new LinkedBlockingQueue<>(PEER_REPLICATOR_BATCH_QUEUE_SIZE);
    /**
     * 第三层队列: 游街队列, batch的同步发送
     */
    private final LinkedBlockingQueue<PeersReplicateBatch> replicateBatchesQueue = new LinkedBlockingQueue<PeersReplicateBatch>(PEER_REPLICATOR_REPLICATE_QUEUE_SIZE);


    @Autowired
    private RegisterServerCluster registerServerCluster;
    private ExecutorService replicateThreadPool;

    public PeersReplicator() {
        // ========================== 第一层队列同步到第二层队列, 定时维护batch到第三层队列 =============================
        // 打batch包的线程
        new Thread() {
            {
                this.setDaemon(true);
            }

            private long latestBatchGeneration = System.currentTimeMillis();

            @Override
            public void run() {
                while (true) {
                    try {
                        BaseClientRequest poll = acceptorQueue.poll();
                        if (poll == null) {
                            // 这里可以用一个condition什么的优化一下.
                            Thread.sleep(100);
                        }
                        currentBatchQueue.offer(poll);

                        // batch是否成型
                        if (System.currentTimeMillis() - latestBatchGeneration >= PEER_REPLICATOR_BATCH_INTERVAL) {
                            if (currentBatchQueue.size() > 0) {
                                replicateBatchesQueue.offer(PeersReplicateBatch.createBatch(currentBatchQueue));
                            }
                            latestBatchGeneration = System.currentTimeMillis();
                        }
                    } catch (InterruptedException e) {
                        log.warn("peers replicator create batch thread was interrupted.");
                    }
                }
            }
        }.start();

        // ============================ 把batch同步到其他 peers那里的 ===============================================
        replicateThreadPool = Executors.newFixedThreadPool(registerServerCluster.getPeers().size());
        // 同步batch包的线程
        new Thread() {
            {
                this.setDaemon(true);
            }

            @Override
            public void run() {
                while (true) {
                    try {
                        PeersReplicateBatch batch = replicateBatchesQueue.poll(PEER_REPLICATOR_BATCH_INTERVAL, TimeUnit.MILLISECONDS);
                        if (batch == null) continue;

                        log.info("start republic batch to peers.");
                        for (String peer : registerServerCluster.getPeers()) {
                            replicateThreadPool.execute(() -> {
                                // TODO imply republic batch to peer
                                log.info("replicate to peer:{}", peer);
                            });
                        }
                    } catch (InterruptedException e) {
                        log.warn("batch replicate thread was interrupted.");
                    }
                }

            }
        }.start();
    }

    // ========================== 第一层队列的插入 ======================================================================

    /**
     * 同步注册请求
     */
    public void replicateRegister(RegisterRequest req) {
        acceptorQueue.offer(req);
    }

    /**
     * 同步取消注册
     */
    public void replicateOffline(RegisterRequest req) {
        acceptorQueue.offer(req);
    }

    /**
     * 同步心跳
     */
    public void replicateHeartbeat(HeartbeatRequest req) {
        acceptorQueue.offer(req);
    }

}
