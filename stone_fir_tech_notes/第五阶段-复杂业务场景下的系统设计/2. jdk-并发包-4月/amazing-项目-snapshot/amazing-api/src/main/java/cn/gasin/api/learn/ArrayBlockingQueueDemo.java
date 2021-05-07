package cn.gasin.api.learn;


import java.util.concurrent.ArrayBlockingQueue;

public class ArrayBlockingQueueDemo {
    public static void main(String[] args) {
        ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(100);

        queue.add(3);
        queue.add(2);
        queue.add(1);
        queue.offer(3);
        queue.offer(2);
        queue.offer(3);

        queue.poll();
        queue.poll();
        queue.poll();
        System.out.println(queue.size());

    }
}
