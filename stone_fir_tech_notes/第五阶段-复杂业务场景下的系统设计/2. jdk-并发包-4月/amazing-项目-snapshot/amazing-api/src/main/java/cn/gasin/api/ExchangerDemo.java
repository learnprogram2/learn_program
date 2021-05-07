package cn.gasin.api;

import java.util.concurrent.Exchanger;

public class ExchangerDemo {

    public static void main(String[] args) throws InterruptedException {
        Exchanger<Integer> integerExchanger = new Exchanger<>();
        integerExchanger.exchange(5);


    }
}
