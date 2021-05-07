package cn.gasin.api;

import lombok.SneakyThrows;

public class ThreadLocalDemo {
    @SneakyThrows
    public static void main(String[] args) {
        ThreadLocal<String> threadLocal = new ThreadLocal<>();

        threadLocal.set("dsafasdf");
        Thread t1 = new Thread(() -> threadLocal.set("thread1哦!"));
        Thread t2 = new Thread(() -> threadLocal.set("thread2呀!"));
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        String s = threadLocal.get();
        System.out.println(s);
    }
//    Thread {
//        ThreadLocalMap {
//            ThreadLocal(requestId): 1L,
//            ThreadLocal(txid): 1L
//        }
//    }

}
