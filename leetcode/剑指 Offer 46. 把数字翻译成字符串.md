[剑指 Offer 46. 把数字翻译成字符串](https://leetcode-cn.com/problems/ba-shu-zi-fan-yi-cheng-zi-fu-chuan-lcof/submissions/)

给定一个数字，我们按照如下规则把它翻译为字符串：0 翻译成 “a” ，1 翻译成 “b”，……，11 翻译成 “l”，……，25 翻译成 “z”。一个数字可能有多个翻译。请编程实现一个函数，用来计算一个数字有多少种不同的翻译方法。

```java
输入: 12258
输出: 5
解释: 12258有5种不同的翻译，分别是"bccfi", "bwfi", "bczi", "mcfi"和"mzi"
```
### 解题思路

字符串连接的可能性: 连在一起就只是一种可能性了;

我的错误想法1: 递推的时候连起来不能用dp[i-2] + 2算, 这个是不对的, 字符串连起来是一种确定的情况, 需要用分步乘法: dp[i-2]*2 才是dp[i-2]对dp[i]的贡献;

我的错误想法2: 递推用 Math.max(dp[i-2]*2, dp[i-1]), 这个也不对, 如果{dp[i-2]*2}大, dp[i-2]*2 并不能代表 dp[i-1]和dp[i-2]对dp[i]的贡献, dp[i-2]*1 是dp[i-2]连接一个xx表示的字符, 另一半dp[i-2]*1表示dp[i-2]+x+x字符, 这个就把dp[i-1]给代表了, 把dp[i-2]给强等于dp[i-2]了.

正确的: 分类加法, 分步乘法. 这个递推应该是先分类后分步:
1. dp[i]分为i是x还是xx, 这两类分别是dp[i-2]的贡献和dp[i-1]对dp[i]的贡献
2. dp[i-2] * 1 是第一类的两步, dp[i-1] * 1 是第二类的两步

![image.png](https://pic.leetcode-cn.com/1605612137-vAladG-image.png)

### 代码

```java
class Solution {
    public int translateNum(int num) {

        String str = num + "";
        if (str.length() < 2) {
            return 1;
        }
        int[] dp = new int[str.length()];
        dp[0] = 1;
        if (str.charAt(0) != 0 && (str.charAt(0) - '0') * 10 + str.charAt(1) - '0' <= 25) {
            dp[1] = 2;
        } else {
            dp[1] = 1;
        }

        for (int i = 2; i < str.length(); i ++) {
            // 本位置作为翻译一定成功;
            // [i-1, i]两位作为翻译;
            if (str.charAt(i - 1) != '0' && (str.charAt(i - 1) - '0') * 10 + str.charAt(i) - '0' <= 25) {
                // ******* 翻译的可能性: 现在就是[i-2]的可能性+[i-1]的可能性.
                dp[i] = dp[i-2]  + dp[i-1];
            } else {
                dp[i] = dp[i-1];
            }

            // ERROR: 我下面的这个{Math.max(dp[i-2] + 2, dp[i-1])} 的意思是 错的
            //          翻译的数量dp[i-2] + 2, 这个是不对的, 意思是 dp[i-2] 和后面两个字符连起来最多构成2种情况, 不能加上dp[i-2].
            // 翻译的可能性, 字符串连在一起就只是1, 不是1+1+1+1....
            // if ((str.charAt(i-1) - '0') * 10 + str.charAt(i) - '0' <= 25) {
            //     dp[i] = Math.max(dp[i-2] + 2, dp[i-1]);
            // } else {
            //     dp[i] = Math.max(dp[i-2], dp[i-1]);
            // }
        }
        return  dp[dp.length - 1];
    }
}
```