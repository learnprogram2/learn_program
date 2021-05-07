package cn.gasin.api;

/**
 * 测试HashMap1.7的并发死锁问题
 */
public class HashMapDemo {
    public static void main(String[] args) {
//        void transfer(Entry[] newTable) {
//            Entry[] src = table;
//            int newCapacity = newTable.length;
//            for (int j = 0; j < src.length; j++) {
//                Entry<K,V> e = src[j];
//                if (e != null) {
//                    src[j] = null;
//                    do {
//                        Entry<K,V> next = e.next;
//                        int i = indexFor(e.hash, newCapacity);
//                        e.next = newTable[i];
//                        newTable[i] = e;
//                        e = next;
//                    } while (e != null);
//                }
//            }
//        }

    }
}
