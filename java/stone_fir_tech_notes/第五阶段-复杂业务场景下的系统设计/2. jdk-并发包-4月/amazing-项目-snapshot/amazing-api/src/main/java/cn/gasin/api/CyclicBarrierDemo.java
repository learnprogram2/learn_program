package cn.gasin.api;

import lombok.SneakyThrows;

import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierDemo {

    @SneakyThrows
    public static void main(String[] args) {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(4);
        int parties = cyclicBarrier.getParties();
        cyclicBarrier.isBroken();
        cyclicBarrier.getNumberWaiting();
        cyclicBarrier.reset();
        cyclicBarrier.await();
    }
}
