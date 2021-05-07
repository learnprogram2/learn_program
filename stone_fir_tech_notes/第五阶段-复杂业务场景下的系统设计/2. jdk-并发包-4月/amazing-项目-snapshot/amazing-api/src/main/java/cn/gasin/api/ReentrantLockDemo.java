package cn.gasin.api;

import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 结论: 1696:1392, 有一些15%的性能提升吧.
 * 比atomicInteger实现递增原子性的优化力度要强.
 */
public class ReentrantLockDemo {
    public static void main(String[] args) throws InterruptedException {
        // reentrantLock和普通synchronized对比:
        ReentrantLockDemo solution = new ReentrantLockDemo();

//        long t1 = System.currentTimeMillis();
//        solution.reentrantLock();
//        long t2 = System.currentTimeMillis();
//        System.out.println(t2 - t1);
//        System.out.println("num1:" + solution.num1);
//
//        solution.synchronizedMethod();
//        System.out.println(System.currentTimeMillis() - t2);
//        System.out.println("num2:" + solution.num2);

        solution.threadSignal.start();
        solution.waitThread.start();
    }

    private Thread threadSignal = new Thread() {
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lock.lock();
            condition.signal();
            lock.unlock();
        }
    };
    private Thread waitThread = new Thread() {
        @Override
        public void run() {
            lock.lock();
            try {
                condition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("await活过来了.");
            lock.unlock();
        }
    };

    private ReentrantLock lock = new ReentrantLock();
    Condition condition = lock.newCondition();
    int num1 = 0;
    int num2 = 0;

    @SneakyThrows
    public void reentrantLock() {
        List<Thread> list = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            list.add(new Thread(() -> {
                lock.lock();
                for (int j = 0; j < 100; j++) {
                    num1++;
                }
                lock.unlock();
            }));
        }
        for (Thread thread : list) {
            thread.start();
        }
        for (Thread thread : list) {
            thread.join();
        }
    }

    @SneakyThrows
    public void synchronizedMethod() {
        List<Thread> list = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            list.add(new Thread(() -> {
                synchronized (lock) {
                    for (int j = 0; j < 100; j++) {
                        num2++;
                    }
                }
            }));
        }
        for (Thread thread : list) {
            thread.start();
        }
        for (Thread thread : list) {
            thread.join();
        }
    }
}
