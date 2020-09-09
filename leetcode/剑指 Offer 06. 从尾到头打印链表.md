[剑指 Offer 06. 从尾到头打印链表](https://leetcode-cn.com/problems/cong-wei-dao-tou-da-yin-lian-biao-lcof/)

输入一个链表的头节点，从尾到头反过来返回每个节点的值（用数组返回）。

```text
输入：head = [1,3,2]
输出：[2,3,1]
```

思路: 题里


```java
        // 1. 递归 √
        // 2. 栈
        // 3. 先计数, 算出int[], 然后顺序遍历, 从后往前填写int[]
    List<Integer> res = new ArrayList<>();
    public int[] reversePrint(ListNode head) {
        if (head == null) {
            return new int[]{};
        }

        reverse(head);

        return res.stream().mapToInt(i->i).toArray();
    }

    public void reverse(ListNode node) {
        if (node.next == null) {
            // 最后一个
            res.add(node.val);
            // 返回就好了
        } else {
            reverse(node.next);
            res.add(node.val);
        }
    }
```