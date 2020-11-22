[剑指 Offer 52. 两个链表的第一个公共节点](https://leetcode-cn.com/problems/liang-ge-lian-biao-de-di-yi-ge-gong-gong-jie-dian-lcof/)

输入两个链表，找出它们的第一个公共节点。

如下面的两个链表：
![](https://assets.leetcode-cn.com/aliyun-lc-upload/uploads/2018/12/14/160_statement.png)
在节点 c1 开始相交。

思路: 容器法, 双指针

```java
public class Solution {
    // public ListNode getIntersectionNode(ListNode headA, ListNode headB) {
    //     // 遍历: 使用容器记录一个链表, 然后遍历另一个链表
    //     Set<ListNode> set = new HashSet<>();
    //     while (headA != null) {
    //         set.add(headA);
    //         headA = headA.next;
    //     }

    //     while (headB != null) {
    //         if (set.contains(headB)) {
    //             return headB;
    //         }
    //         headB = headB.next;
    //     }
    //     return null;
    // }

    public ListNode getIntersectionNode(ListNode headA, ListNode headB) {
        // 双指针: 1. 如果link1和link2相同长度, 那么从头遍历, 会相遇的.
        //        2. 如果不同长度, 先遍历一遍, 先到头的从头开始, 换一个链表接着遍历, 然后第二个到头的也从另一个链表接着遍历. 最终相遇
        ListNode la = headA;
        ListNode lb = headB;
        while (la != null && lb != null) {
            if (la == lb) {
                return la;
            }
            la = la.next;
            lb = lb.next;
        }
        if (la == null) {
            la = headB;
        } else if (lb == null) {
            lb = headA;
        }
        while (la != null && lb != null) {
            la = la.next;
            lb = lb.next;
        }
        if (la == null) {
            la = headB;
        } else if (lb == null) {
            lb = headA;
        }
        while (la != null && lb != null) {
            if (la == lb) {
                return la;
            }
            la = la.next;
            lb = lb.next;
        }
        return null;
    }
}
```