[剑指 Offer 35. 复杂链表的复制](https://leetcode-cn.com/problems/fu-za-lian-biao-de-fu-zhi-lcof/)

请实现 copyRandomList 函数，复制一个复杂链表。在复杂链表中，每个节点除了有一个 next 指针指向下一个节点，还有一个 random 指针指向链表中的任意节点或者 null。


思路: 
    // 我记得有一个方法是先重复每一个节点, 然后去除掉原来的节点.
    // 还可以用hash表, 复制出来另一个维度里做链接, 不需要去除节点了. 我看第一个题解的DFSBFS什么的也是Hash表基础做的.
    // note: 不能修改原来的链表
```java
/*
// Definition for a Node.
class Node {
    int val;
    Node next;
    Node random;

    public Node(int val) {
        this.val = val;
        this.next = null;
        this.random = null;
    }
}
*/
class Solution {
    // 我记得有一个方法是先重复每一个节点, 然后去除掉原来的节点.
    // 还可以用hash表, 复制出来另一个维度里做链接, 不需要去除节点了. 我看第一个题解的DFSBFS什么的也是Hash表基础做的.
    // note: 不能修改原来的链表

    
    public Node copyRandomList(Node head) {
        if (head == null) {
            return null;
        }
        // 先遍历, 复制, 理顺next指针
        Node temp = head;
        while (temp != null) {
            Node dump = new Node(temp.val);
            dump.next = temp.next;
            temp.next = dump;
            temp = temp.next.next;
        }
        // 搞好dump的random指针
        temp = head;
        while (temp != null) {
            Node dump = temp.next;
            if (temp.random != null) {
                dump.random = temp.random.next;
            }
            temp = temp.next.next;
        }
        // 干掉原来的node, 理清next节点
        Node res = head.next;
        temp = head;
        while (temp != null) {
            Node dump = temp.next;
            temp.next = dump.next;
            if (temp.next != null) {
                dump.next = temp.next.next;
            }
            temp = temp.next;
        }
        return res;
    }
}
```