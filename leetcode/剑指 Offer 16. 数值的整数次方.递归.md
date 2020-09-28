[剑指 Offer 16. 数值的整数次方](https://leetcode-cn.com/problems/shu-zhi-de-zheng-shu-ci-fang-lcof/)

实现函数double Power(double base, int exponent)，求base的exponent次方。不得使用库函数，同时不需要考虑大数问题。

```text
输入: 2.00000, 10
输出: 1024.00000
```

思路: 
1. 简单的计算, 会超时
2. 递归, 二分. 要注意边界

```java
class Solution {
    public double myPow(double x, int n) {
        if (n == 0) {
            return 1;
        } else if (n < 0) {
            // n <0, 把x变成1/x, 然后, 把-n个提出一个(1/x), 因为int.MIN_VALUE的相反数还是自己出现死循环, 把n减小一个, 开启健康递归.
            return 1 / x * myPow( 1 / x ,  - n - 1);
            
            // ERROR1: 下面这个因为int.MIN_VALUE负数还是自己, 会变成无限循环.
            // return myPow( 1 / x ,  - n);
            
            // ERROR2: 下面这两行因为相反数还是负数, 发生错位, 会把x变成>1的, 然后n变成正数, 变成无限大.
            // x = 1 / x;
            // n = - n;
        } else if (n == 1) {
            return x;
        }

        // 递归去调用自己:
        if ( n % 2 == 0 ) {
            return myPow(x * x, n / 2);
        }
        return x * myPow(x * x, n / 2);
    }

    // 这是我写的, 超时了.
    // public double myPow(double x, int n) {
    //     if (x == 1) {
    //         return 1;
    //     }
    //     double res = 1;
    //     boolean up = n < 0;
    //     while(n != 0) {
    //         res *= x;
    //         n -= up ? 1 : -1;
    //     }
    //     return up ? res : 1 / res;
    // }
}
```


