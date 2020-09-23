[剑指 Offer 14- I. 剪绳子](https://leetcode-cn.com/problems/jian-sheng-zi-lcof/)


给你一根长度为 n 的绳子，请把绳子剪成整数长度的 m 段（m、n都是整数，n>1并且m>1），每段绳子的长度记为 k[0],k[1]...k[m-1] 。请问 k[0]*k[1]*...*k[m-1] 可能的最大乘积是多少？例如，当绳子的长度是8时，我们把它剪成长度分别为2、3、3的三段，此时得到的最大乘积是18。

```text
输入: 2
输出: 1
解释: 2 = 1 + 1, 1 × 1 = 1
```

思路: 至少要分成两半



```java
class Solution {
    public int cuttingRope(int n) {
        // 动态规划: 
        int[] arr = new int[n + 1];
        arr[0] = 0;
        arr[1] = 1;
        arr[2] = 1;
        for (int i = 2; i <= n; i ++) {
            // i 分成 2 个部分(因为至少要两分),  左右两个部分里最大的的乘积 乘起来来最大
            for (int j = 1; j < i; j ++) {
                // [0,j] 是一个拆分, 然后 [i-j, i]是一个拆分: 就会有
                arr[i] = Math.max(arr[j] * arr[i-j], arr[i]);  // 左拆, 右拆
                arr[i] = Math.max(arr[j] * (i-j), arr[i]);     // 左拆, 右不拆
                arr[i] = Math.max(j * arr[i-j], arr[i]);       // 左不拆, 右拆
                arr[i] = Math.max(j * (i-j), arr[i]);          // 左不拆, 右不拆
                
                // 不能不拆 arr[i] = Math.max(i, arr[i]);
            }
        }
        return arr[n];
    }
}
```








