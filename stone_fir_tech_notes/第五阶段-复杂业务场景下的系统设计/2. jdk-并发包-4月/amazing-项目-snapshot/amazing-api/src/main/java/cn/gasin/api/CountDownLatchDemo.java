package cn.gasin.api;

import lombok.SneakyThrows;

import java.util.concurrent.CountDownLatch;

/**
 *
 */
public class CountDownLatchDemo {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(3);
        new Thread() {
            @SneakyThrows
            @Override
            public void run() {
                // countDownLatch.countDown();
                countDownLatch.await();
                System.out.println("444");
            }
        }.start();
        new Thread() {
            @SneakyThrows
            @Override
            public void run() {
                // countDownLatch.countDown();
                countDownLatch.await();
                System.out.println("555");
            }
        }.start();



        System.out.println(countDownLatch.getCount());
        new Thread() {
            @Override
            public void run() {
                countDownLatch.countDown();
                System.out.println("111");
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                countDownLatch.countDown();
                System.out.println("222");
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                countDownLatch.countDown();
                System.out.println("333");
            }
        }.start();

        countDownLatch.await();
        System.out.println("await 唤醒了");
    }
}
