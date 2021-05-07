[143. 重排链表](https://leetcode-cn.com/problems/reorder-list/solution/fan-zhuan-pin-jie-by-wangyk-ia1h/)

给定一个单链表 L：L0→L1→…→Ln-1→Ln ，
将其重新排列后变为： L0→Ln→L1→Ln-1→L2→Ln-2→…

你不能只是单纯的改变节点内部的值，而是需要实际的进行节点交换。

```java
给定链表 1->2->3->4, 重新排列为 1->4->2->3.
```

思路:
1. 使用list把链表固化, 然后操作
2. 反转后面的链表
3. 优先队列, 这个比较傻, 定制node写priority, 直接放进, 然后取出来.




