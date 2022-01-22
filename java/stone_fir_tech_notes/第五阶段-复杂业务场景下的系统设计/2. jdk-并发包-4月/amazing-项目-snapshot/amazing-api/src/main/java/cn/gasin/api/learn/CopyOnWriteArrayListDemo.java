package cn.gasin.api.learn;

import java.util.concurrent.CopyOnWriteArrayList;

public class CopyOnWriteArrayListDemo {

    public static void main(String[] args) {
        CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<>();
        list.add(5);
        list.add(3);
        list.add(6);
        list.get(2);
    }


}
