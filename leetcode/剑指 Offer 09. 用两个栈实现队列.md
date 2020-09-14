[剑指 Offer 09. 用两个栈实现队列](https://leetcode-cn.com/problems/yong-liang-ge-zhan-shi-xian-dui-lie-lcof/)

用两个栈实现一个队列。队列的声明如下，请实现它的两个函数 appendTail 和 deleteHead ，分别完成在队列尾部插入整数和在队列头部删除整数的功能。(若队列中没有元素，deleteHead 操作返回 -1 )

```text
输入：
["CQueue","appendTail","deleteHead","deleteHead"]
[[],[3],[],[]]
输出：[null,null,3,-1]
```

思路: 很简单哦

```java
class CQueue {
    private Stack<Integer> tail = new Stack<>();
    private Stack<Integer> head = new Stack<>();

    public CQueue() {}
    
    public void appendTail(int value) {
        tail.push(value);
    }
    
    public int deleteHead() {
        if (tail.size() == 0 && head.size() == 0) {
            return -1;
        } else if (head.size() == 0) {
            // 把tail的挪到head里
            while(tail.size() > 0){
                head.push(tail.pop());
            }
        }
        
        return head.pop();
    }
}
```