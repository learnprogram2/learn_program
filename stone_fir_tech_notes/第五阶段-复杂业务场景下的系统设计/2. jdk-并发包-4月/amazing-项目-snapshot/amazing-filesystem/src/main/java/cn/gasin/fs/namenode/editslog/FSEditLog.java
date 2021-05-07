package cn.gasin.fs.namenode.editslog;

import lombok.extern.log4j.Log4j2;

/**
 * maintain edit-log.
 * 专门负责管理写入edits log到磁盘文件里去
 * <p>
 * 51_案例实战：基于synchronized实现edits log的分段加锁机制
 * 52_案例实战：基于wait与notify实现edits log批量刷磁盘
 */
@Log4j2
public class FSEditLog {

    /// 这个是全局的txid
    private long txidSeq = 0L;

    // 缓冲区
    private final DoubleBuffer doubleBuffer;
    // 正在sync
    private volatile boolean isSyncing = false;
    // 需要sync
    private volatile boolean isWaitSync = false;
    // 同步的最大txid
    private volatile Long syncMaxTxid = 0L;

    // 分段: 不同线程有自己的id
    private ThreadLocal<Long> localTxid = new ThreadLocal<>();

    public FSEditLog() {
        this.doubleBuffer = new DoubleBuffer();
    }


    /**
     * Write an operation to the edit log
     * 写log到缓冲区里面.
     */
    public void logEdit(String log) {
        synchronized (this) {
            long txid = ++txidSeq;
            EditLog editLog = new EditLog(txid, log);
            doubleBuffer.write(editLog);
            // 更新当前线程的最大txid
            localTxid.set(txid);
        }

        syncBuffer(); // trigger一下
    }

    /**
     * 尝试把buffer的数据刷一下
     * 1. 使用synchronized来保证并发.
     */
    private void syncBuffer() {
        synchronized (this) {
            // 有其他线程正在sync: 必须要进来看一下, 来保证最后一点数据也要刷出去.
            if (isSyncing) {
                // 当前线程的txid都已经刷好了
                if (localTxid.get() < syncMaxTxid) {
                    return;
                }
                // 如果有别的线程在等, 直接返回.
                if (isWaitSync) {
                    return;
                }
                isWaitSync = true;
                while (isSyncing) {
                    try {
                        wait(2000);
                    } catch (InterruptedException e) {
                        // 被打断也要刷剩下的一点福根.
                        log.error(e);
                    }
                }
                isWaitSync = false;
            }

            // 交换两块buffer
            doubleBuffer.readyToSync();
            // 记下来当前的刷数据状态
            syncMaxTxid = doubleBuffer.getSyncBufferLatest();
            isSyncing = true;
        }

        // 开始flush: 这个放在锁外面, 因为很耗时.
        doubleBuffer.flush();

        synchronized (this) {
            isSyncing = false;
            // 把大家都叫醒.
            notifyAll();
        }
    }

}
