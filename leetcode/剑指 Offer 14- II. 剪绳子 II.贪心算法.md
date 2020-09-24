[剑指 Offer 14- II. 剪绳子 II](https://leetcode-cn.com/problems/jian-sheng-zi-ii-lcof/)

给你一根长度为 n 的绳子，请把绳子剪成整数长度的 m 段（m、n都是整数，n>1并且m>1），每段绳子的长度记为 k[0],k[1]...k[m - 1] 。请问 k[0]*k[1]*...*k[m - 1] 可能的最大乘积是多少？例如，当绳子的长度是8时，我们把它剪成长度分别为2、3、3的三段，此时得到的最大乘积是18。

答案需要取模 1e9+7（1000000007），如计算初始结果为：1000000008，请返回 1。

思路: 贪心算法, 贪心算法是个毛啊.

贪心着眼现实当下，动规谨记历史进程。



```java
class Solution {
    public int cuttingRope(int n) {
        // 贪心算法
        if (n == 2) {
            return 1;
        } else if (n == 3) {
            return 2;
        }

        // 在n大于等于4的时候开始拆分
        // 优先拆分3 (n>4, 如果n=4, 那么就22)
        long res = 1;
        while (n > 4) {
            res *= 3;
            res %= 1000000007;
            n -= 3;
        }
        // 最后剩下 n (n=4|2|1), 不用拆分2了
        res *= n;
        res %= 1000000007;

        return (int)(res);
    }
}
```