package cn.gasin.api;

import lombok.Getter;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicIntegerDemo {

    public static void main(String[] args) {
        AtomicIntegerDemo demo = new AtomicIntegerDemo();
        long t1 = System.currentTimeMillis();
        demo.atomicAdd();
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        demo.synchronizedAdd();
        long t3 = System.currentTimeMillis();
        System.out.println(t3 - t2);
        System.out.println(demo.getAtomicI().get());
        System.out.println(demo.getI());
    }

    @Getter
    private AtomicInteger atomicI = new AtomicInteger(0);
    @Getter
    private int i = 0;


    // 这个会慢一点, 慢的不多. 就大概1/10不到的样子.(并发量10000+) 几乎没区别.
    @SneakyThrows
    public synchronized void synchronizedAdd() {
        ArrayList<Thread> threads = new ArrayList<>(100);
        for (int j = 0; j < 10000; j++) {
            threads.add(new Thread() {
                @Override
                public void run() {
                    for (int i1 = 0; i1 < 1000; i1++) {
                        synchronized (AtomicIntegerDemo.class) {
                            i++;
                        }
                    }
                }
            });
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }

    @SneakyThrows
    public void atomicAdd() {
        ArrayList<Thread> threads = new ArrayList<>(100);
        for (int j = 0; j < 10000; j++) {
            threads.add(new Thread() {
                @Override
                public void run() {
                    for (int i1 = 0; i1 < 1000; i1++) {
                        atomicI.incrementAndGet();
                    }
                }
            });
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
    }


}
