package cn.gasin.api;

import java.util.concurrent.Semaphore;

public class SemaphoreDemo {
    public static void main(String[] args) throws InterruptedException {
        // 创建semaphore, 设置初始的state
        Semaphore semaphore = new Semaphore(5);
        // 扣减state, 如果state>1, 拿state成功, 就不阻塞, 如果state不够, 就阻塞.
        //      阻塞醒来之后还是tryAcquire,尝试扣减state, 如果.... 循环.
        semaphore.acquire();
        // 增加state, 然后把队列里的node叫醒.
        semaphore.release();

        semaphore.drainPermits();

    }
}
