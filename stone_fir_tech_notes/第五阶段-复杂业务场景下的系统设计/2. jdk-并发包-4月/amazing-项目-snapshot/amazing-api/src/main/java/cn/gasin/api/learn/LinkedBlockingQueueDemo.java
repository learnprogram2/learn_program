package cn.gasin.api.learn;

import java.util.concurrent.LinkedBlockingQueue;

public class LinkedBlockingQueueDemo {
    public static void main(String[] args) {
        LinkedBlockingQueue<Integer> blockingQueue = new LinkedBlockingQueue<>();
        blockingQueue.offer(1);
        blockingQueue.poll();
        blockingQueue.peek();
        blockingQueue.iterator();
        blockingQueue.size();


    }
}
