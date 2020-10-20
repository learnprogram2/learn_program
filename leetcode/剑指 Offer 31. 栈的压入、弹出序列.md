[剑指 Offer 31. 栈的压入、弹出序列](https://leetcode-cn.com/problems/zhan-de-ya-ru-dan-chu-xu-lie-lcof/)

输入两个整数序列，第一个序列表示栈的压入顺序，请判断第二个序列是否为该栈的弹出顺序。假设压入栈的所有数字均不相等。

例如，序列 {1,2,3,4,5} 是某栈的压栈序列，序列 {4,5,3,2,1} 是该压栈序列对应的一个弹出序列，但 {4,3,5,1,2} 就不可能是该压栈序列的弹出序列。

思路: 步骤重演, 建立在数字不相同的基础上:

如果数字有相同的, 我觉得应该要DFS, 开始分叉了.

```java
    public boolean validateStackSequences(int[] pushed, int[] popped) {
        if (pushed == null || popped == null || pushed.length != popped.length) {
            return false;
        }
        // 根据pushed, popped重现, 判断最后popped能不能清空: 关键: 压入栈的数字不相等.
        Stack<Integer> stack = new Stack<>();
        int popI = 0;
        for (int i = 0; i < pushed.length; i++) {
            // 先push进去
            stack.push(pushed[i]);

            // 看看能不能弹
            while (stack.size() > 0 && stack.peek() == popped[popI]) {
                stack.pop();
                popI++;
            }
        }
        return popI == popped.length;
    }
```