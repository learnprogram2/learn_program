## [剑指 Offer 10- II. 青蛙跳台阶问题](https://leetcode-cn.com/problems/qing-wa-tiao-tai-jie-wen-ti-lcof/)

一只青蛙一次可以跳上1级台阶，也可以跳上2级台阶。求该青蛙跳上一个 n 级的台阶总共有多少种跳法。

答案需要取模 1e9+7（1000000007），如计算初始结果为：1000000008，请返回 1。

思路: 就是动态规划



```java
class Solution {
    public int numWays(int n) {
        if (n <= 1){
            return 1;
        }
        // 动态规划
        int[] res = new int[n];
        res[0] = 1;
        res[1] = 2;
        for (int i = 2; i < n; i ++) {
            res[i] = (res[i-2] + res[i-1]) % 1000000007;
        }
        return res[n-1];
    }
}
```

