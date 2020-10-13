定义一个函数，输入一个链表的头节点，反转该链表并输出反转后链表的头节点。

```java
输入: 1->2->3->4->5->NULL
输出: 5->4->3->2->1->NULL
```

思路: 前后拿着指针, 用另一个指针负责改变中间的node. 很容易绕晕, 记住就三个part就好了: 不要搞出4个变量.

```java
/**
 * Definition for singly-linked list.
 * public class ListNode {
 *     int val;
 *     ListNode next;
 *     ListNode(int x) { val = x; }
 * }
 */
class Solution {
    public ListNode reverseList(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        }

        ListNode temp = head;
        head = head.next;
        temp.next = null;

        while (head.next != null) {
        // 这四步我绕晕了当时, 要记住 前后两个, 然后中间一个桥梁就好了.
            ListNode l = head;
            head = head.next;
            l.next = temp;
            temp = l;
        }

        head.next = temp;

        return head;
    }
}
```