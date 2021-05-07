## 集合

JDK集合源码，分成3个步骤来走，第一个是图解原理，第二个是弄一个模拟的案例来用一用，第三个是分析里面的源码实现



## ArrayList

优点: 读取快, 修改快

缺点: 增删慢, 扩容比较慢

元素大量的移动，数组的扩容+元素的拷贝 尽量不要用.

顺序遍历可以.

1. **普通添加**

   ```java
   private void add(E e, Object[] elementData, int s) {
       // 如果满了, 就grow() , 正常就是扩容到原来的3/2.
       if (s == elementData.length)
           elementData = grow();
       // 放在对应位子上, 然后把s指针往上添加
       elementData[s] = e;
       size = s + 1;
   }
   ```

2. **修改set(index, value)**

   ```java
   public E set(int index, E element) {
       // 先检查一下index要在范围内
       Objects.checkIndex(index, size);
       // 先把oldValue取出来, 然后设置上新的value, 把oldValue返回就好了.
       E oldValue = elementData(index);
       elementData[index] = element;
       return oldValue;
   }
   ```

3. **指定index添加add(index,value)**

   ```java
       public void add(int index, E element) {
           // 这个add必须是合法的( 在size里面)
           rangeCheckForAdd(index);
           modCount++;
           final int s;
           Object[] elementData;
           // 如果array满了, 就扩容
           if ((s = size) == (elementData = this.elementData).length)
               elementData = grow();
           // 把[index, size-1]往后挪一位, 然后把index位放上我们要的数据.
           System.arraycopy(elementData, index,
                            elementData, index + 1,
                            s - index);
           elementData[index] = element;
           size = s + 1;
       }
   ```

4. **remove(index) -不会缩容**

   ```java
   // 把[index, size-1]的数组往前挪一位.
   ```

   





## LinkedList

- **插入删除比较快**

查找修改比较慢, 要找. **适合频繁插入删除的场景队列什么的**.

- **双向链表实现, 继承Dequeue**

  ![01_LinkedList数据结构](jdk%E9%9B%86%E5%90%88.assets/01_LinkedList%E6%95%B0%E6%8D%AE%E7%BB%93%E6%9E%84.png)





## Vector&Stack

- Stack继承Vector, vector类似于Arraylist, 是Obj[]实现的
- **vector每次扩容2倍**





## HashMap

- array+link/红黑树, 初始容量16.
- load factor: 0.75 扩容界限
- Node[] 存放hash表.

1. **hash算法: **

   obj.hash ^ (obj.hash >>> 16) 

   **低16位包括了高16位和低16位的特征.** 因为后面数组index定位的时候经常用到低位. 也会很好的包含特征, 降低hash冲突.

   ```java
   // 比如插入的时候, 会用table的length和hash取&, 来找index.        
   if ((p = tab[i = (n - 1) & hash]) == null)
               tab[i] = newNode(hash, key, value, null);
   ```

2. **put操作和hash寻址算法:**

   - 普通的寻址: `(p = tab[i = (n - 1) & hash]) == null` 取&与运算, 而不是取模.

3. **hash冲突的链表处理**

   再加一个超过8就变成红黑树.

4. **红黑树解决hash冲突**

   添加上一个node之后, 链表超过了8, 就把链表修改成双向链表TreeNode, 然后转成红黑树.

5. **数组扩容** resize();

   - 2倍扩容 
   - **rehash: `(n - 1) & hash`, 因为n-1增加了一个1, 那么算出来的index, 要么在原位置, 要么增加了一个n/2(n为2的指数, 所以n-1的最高位为n/2)**
   - 链表更好弄了, 弄两个link, 开始遍历, 重新hash.

6. **remove()**

   正常的计算hash, 定位node位置, 然后干掉它.









## LinkedHashMap

- **继承HashMap, 实现三个狗子方法, 来实现增加删除的链表更新:**

  ```
  void afterNodeAccess(Node<K,V> p) { }
  void afterNodeInsertion(boolean evict) { }
  void afterNodeRemoval(Node<K,V> p) { }
  ```

- **继承Node, 添加两个字段, 实现link**

  ```java
  class Entry<K,V> extends HashMap.Node<K,V> {
      Entry<K,V> before, after;
      Entry(int hash, K key, V value, Node<K,V> next) {
          super(hash, key, value, next);
      }
  }
  ```

- ```
  accessOrder标记来记录读取的时候要不要更新link的顺序, true是会更新, 不过默认是false.
  ```









## TreeMap 

红黑树: 排序树, 左小右大, 插入子节点然后平衡.

```java
static final class Entry<K,V> implements Map.Entry<K,V> {
    K key;
    V value;
    Entry<K,V> left;
    Entry<K,V> right;
    Entry<K,V> parent;
    boolean color = BLACK;
}
```











## 各种Set: HashSet-LinkedHashSet-TreeSet

- **HashSet**: 维护一个HashMap, 直接set进去.
- **LinkedHashSet**: 继承HashSet, 把hashMap换成LinkedHashMap. 然后什么都不用干了. 直接HashSet的方法, 所以默认是实现了插入的顺序, 还可以读改也更新link顺序.
- **TreeSet**: 维护一个TreeMap, 红黑树, 是有顺序的.
- 这三个Set用的**map里的value设置成同一个Object. 占着位置.**







## Iterator的线程安全fail_fast机制

- keySet()方法, 实际上是创建了一个新的Iterator, 这个iterator记录了当前map的modCount, 每次遍历前都会校验这个modCount有没有变, 变了就马上ConcurrentModificationException

![image-20210413142620951](jdk%E9%9B%86%E5%90%88.assets/image-20210413142620951.png)































