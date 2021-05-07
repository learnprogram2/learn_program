package cn.gasin.api.learn;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ConcurrentLinkedQueueDemo {
    public static void main(String[] args) {
        ConcurrentLinkedQueue<Integer> integerQueue = new ConcurrentLinkedQueue<>();
        integerQueue.add(3);
        integerQueue.offer(5);
        integerQueue.poll();

    }
}
