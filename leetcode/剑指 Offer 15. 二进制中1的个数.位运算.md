[剑指 Offer 15. 二进制中1的个数.位运算](https://leetcode-cn.com/problems/er-jin-zhi-zhong-1de-ge-shu-lcof//)

请实现一个函数，输入一个整数，输出该数二进制表示中 1 的个数。例如，把 9 表示成二进制是 1001，有 2 位是 1。因此，如果输入 9，则该函数输出 2。

```text
输入：00000000000000000000000000001011
输出：3
解释：输入的二进制串 00000000000000000000000000001011 中，共有三位为 '1'。
```

思路: 
把n右移, 就不用考虑符号的问题了, 因为把1左移, 移动到最高位还不是0了.

```java
public class Solution {
    // you need to treat n as an unsigned value
    public int hammingWeight(int n) {
        int res = 0;
        for ( int i = 0; i < 32; i ++) {
            res += (n >> i) & 1;
        }
        return res;
    }
}
```