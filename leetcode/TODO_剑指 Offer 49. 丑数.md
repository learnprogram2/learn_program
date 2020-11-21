[剑指 Offer 49. 丑数](https://leetcode-cn.com/problems/chou-shu-lcof/)

我们把只包含质因子 2、3 和 5 的数称作丑数（Ugly Number）。求按从小到大的顺序的第 n 个丑数。

```java
输入: n = 10
输出: 12
解释: 1, 2, 3, 4, 5, 6, 8, 9, 10, 12 是前 10 个丑数。
```

思路: 我的是遍历方法:
1. 第一步从一开始计数, 直到找到第n个丑数
2. 判断丑数: i能被从[6, i-1]的非丑数整除的, 而且i不能是质数.

代码: 
```java

class Solution {
    // public int nthUglyNumber(int n) {
    //     // 没有思路, 只有遍历
    //     // 应该可以用cache什么的
        
    //     int sum = 1;
    //     int curr = 1;
    //     while (sum < n) {
    //         // 判断curr是不是丑数
    //         if (ugly(curr)) {
    //             sum ++;
    //         }
    //         curr ++;
    //     }
    //     return curr;
    // }
    // // ERROR: 这里是在判断质数了..
    // public boolean ugly(int num) {
    //     if (num < 4) {
    //         return true;
    //     }

    //     if (num % 4 == 0) {
    //         return false;
    //     }
    //     for (int i = 6; i < num; i ++) {
    //         if (num % i == 0) {
    //             return false;
    //         }
    //     }
    //     return true;
    // }

    // public int nthUglyNumber(int n) {
    //     // 第二次: 看到了动态规划四个字, 感觉应该做一做. : 乱了不知道该怎么判断什么是丑数.
    //     List<Boolean> list = new ArrayList<>(n);

    //     int sum = 1;
    //     int curr = 1;
    //     while (sum < n) {
    //         // 判断curr是不是丑数
    //         if (ugly(curr, list)) {
    //             sum ++;
    //             list.add(true);
    //         } else {
    //             list.add(false);
    //         }
    //         curr ++;
    //     }
    //     return curr;
    // }
    // public boolean ugly(int num, List<Boolean> list) {
    //     if (num <= 6) {
    //         return true;
    //     }

    //     for (int i = 7; i < num; i ++) {
    //         if (num % i == 0) {
    //             // 检查 num/i 和 i 是不是丑数, 如果有一个不是就不是
    //             if (list.get(num / i - 1) && list.get(i - 1)){
    //                 return true;
    //             } 
    //         }
    //     }
    //     return false;
    // }

    // 第二次优化:
    public int nthUglyNumber(int n) {
        // 第二次: 看到了动态规划四个字, 感觉应该做一做.
        List<Boolean> list = new ArrayList<>(n);

        int sum = 1;
        int curr = 1;
        while (sum <= n) {
            // 判断curr是不是丑数
            if (ugly(curr, list)) {
                sum++;
                list.add(true);
            } else {
                list.add(false);
            }
            curr++;
        }
        return curr - 1;
    }

    public boolean ugly(int num, List<Boolean> list) {
        if (num <= 6) {
            return true;
        }

        boolean zhi = true;
        for (int i = 2; i < num; i++) {
            if (num % i == 0) {
                // 检查 num/i 和 i 是不是丑数, 如果有一个不是就不是
                if (!(list.get(num / i - 1) && list.get(i - 1))) {
                    return false;
                }
                zhi = false;
            }
        }
        if (zhi) {
            return false;
        }
        return true;
    }
}
```