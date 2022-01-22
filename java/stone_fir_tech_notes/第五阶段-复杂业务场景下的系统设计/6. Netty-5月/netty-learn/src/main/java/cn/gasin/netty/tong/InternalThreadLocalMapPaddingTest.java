package cn.gasin.netty.tong;

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.FastThreadLocalThread;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class InternalThreadLocalMapPaddingTest {

    /**
     * 单线程运行20次
     */
    @Test
    public void testPadding() {
        List<FastThreadLocal<Boolean>> list = new ArrayList<FastThreadLocal<Boolean>>();
        for (int i = 0; i < 1000000; i++) {
            list.add(new FastThreadLocal<Boolean>());
        }

        Boolean value = Boolean.TRUE;
        for (int j = 0; j < 20; j++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000000; i++) {
                FastThreadLocal<Boolean> fastThreadLocal = list.get(i);
                fastThreadLocal.set(value=!value);
            }
            for (int i = 0; i < 1000000; i++) {
                FastThreadLocal<Boolean> fastThreadLocal = list.get(i);
                fastThreadLocal.get();
            }
            FastThreadLocal.removeAll();
            long end = System.currentTimeMillis();
            System.out.println(end - start);
        }

    }

    /**
     * 多线程运行20个线程
     */
    @Test
    public void testPaddingMultiThread() throws InterruptedException {
        final List<FastThreadLocal<Boolean>> list = new ArrayList<FastThreadLocal<Boolean>>();
        for (int i = 0; i < 100000; i++) {
            list.add(new FastThreadLocal<Boolean>());
        }

        final CountDownLatch countDownLatch = new CountDownLatch(20);
        for (int k = 0; k < 20; k++) {
            new FastThreadLocalThread(new Runnable() {
                @Override
                public void run() {
                    long start = System.currentTimeMillis();
                    Boolean value = Boolean.TRUE;
                    for (int i = 0; i < 100000; i++) {
                        FastThreadLocal<Boolean> fastThreadLocal = list.get(i);
                        fastThreadLocal.set(value=!value);
                    }
                    for (int i = 0; i < 100000; i++) {
                        FastThreadLocal<Boolean> fastThreadLocal = list.get(i);
                        fastThreadLocal.get();
                    }
                    FastThreadLocal.removeAll();
                    long end = System.currentTimeMillis();
                    System.out.println(end - start);

                    countDownLatch.countDown();
                }
            }).start();
        }
        countDownLatch.await();
        System.out.println("finished");
    }

}
