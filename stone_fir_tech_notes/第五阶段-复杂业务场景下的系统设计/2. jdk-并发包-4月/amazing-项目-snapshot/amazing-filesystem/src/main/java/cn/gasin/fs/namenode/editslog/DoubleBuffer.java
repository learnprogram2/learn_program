package cn.gasin.fs.namenode.editslog;

import lombok.extern.log4j.Log4j2;

import java.util.LinkedList;

/**
 * 内存的缓冲区: 准备两块缓冲, 交替着来.
 * 1. 为输出到磁盘的缓存
 */
@Log4j2
public class DoubleBuffer {

    LinkedList<EditLog> currentBuffer;
    LinkedList<EditLog> syncBuffer;

    public DoubleBuffer() {
        currentBuffer = new LinkedList<>();
        syncBuffer = new LinkedList<>();
    }

    public void write(EditLog editLog) {
        currentBuffer.add(editLog);
    }

    /**
     * 交换两个buffer
     * FIXME: 两个缓冲的交换, 有并发问题, 还没有解决.
     */
    public void readyToSync() {
        LinkedList<EditLog> temp = currentBuffer;
        currentBuffer = syncBuffer;
        syncBuffer = temp;
    }

    /**
     * 把准备好的一块缓冲区数据刷到磁盘
     */
    public void flush() {
        for (EditLog editLog : syncBuffer) {
            log.info("flush editLog to disk:{}", editLog);
            // TODO: implements flush logic.
        }
        syncBuffer.clear();
        log.info("flush success");
    }

    public Long getSyncBufferLatest() {
        return syncBuffer.getLast() == null ? null : syncBuffer.getLast().getTxid();
    }
}
