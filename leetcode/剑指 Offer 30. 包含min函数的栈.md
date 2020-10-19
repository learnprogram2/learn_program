[剑指 Offer 30. 包含min函数的栈](https://leetcode-cn.com/problems/bao-han-minhan-shu-de-zhan-lcof/)

定义栈的数据结构，请在该类型中实现一个能够得到栈的最小元素的 min 函数在该栈中，调用 min、push 及 pop 的时间复杂度都是 O(1)。

```
MinStack minStack = new MinStack();
minStack.push(-2);
minStack.push(0);
minStack.push(-3);
minStack.min();   --> 返回 -3.
minStack.pop();
minStack.top();      --> 返回 0.
minStack.min();   --> 返回 -2.
```

思路: 维护一个递减栈, (前提是每个元素都不一样. 如果不能保证, 就是非递增栈)

```java
class MinStack {

    // 维护一个递减栈, (前提是每个元素都不一样. 如果不能保证, 就是非递增栈)
    Stack<Integer> min = new Stack<>();
    Stack<Integer> stack = new Stack<>();

    /** initialize your data structure here. */
    public MinStack() {
    }
    
    public void push(int x) {
        stack.push(x);
        if (min.size() > 0 && min.peek() >= x) {
            min.push(x);
        } else if (min.size() == 0) {
            min.push(x);
        }
    }
    
    public void pop() {
        if (stack.size() > 0 && min.peek().equals(stack.pop())) {
            min.pop();
        }
    }
    
    public int top() {
        return stack.peek();
    }
    
    public int min() {
        return min.size() > 0 ? min.peek() : -1;
    }
}

/**
 * Your MinStack object will be instantiated and called as such:
 * MinStack obj = new MinStack();
 * obj.push(x);
 * obj.pop();
 * int param_3 = obj.top();
 * int param_4 = obj.min();
 */
```


