package cn.gasin.api;

import java.util.LinkedList;

/**
 * 内存队列: 简单的生产者和消费者.
 */
public class QueueInCache {

    LinkedList<Integer> queue;
    int size = -1;

    public QueueInCache(int size) {
        queue = new LinkedList<>();
        this.size = size;
    }

    public synchronized void offer(int value) throws InterruptedException {
        if (queue.size() == this.size) {
            wait();
        }
        //
        queue.offer(value);
        notify();
    }

    public synchronized int poll() throws InterruptedException {
        while (true) {
            Integer val = queue.poll();
            if (val == null) {
                wait();
            } else {
                notify();
                return val;
            }
        }
    }


}
