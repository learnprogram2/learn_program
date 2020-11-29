[剑指 Offer 44. 数字序列中某一位的数字](https://leetcode-cn.com/problems/shu-zi-xu-lie-zhong-mou-yi-wei-de-shu-zi-lcof/solution/zhao-gui-lu-die-die-zhuang-zhuang-mo-suo-bian-jie-/)

思路: 照着下面图开始往后排n的位置, 找到n的数, 然后找到n在该数上的index, 变成String找就好了.
![image.png](https://pic.leetcode-cn.com/1605178898-rqbgia-image.png)


### 代码

```java
class Solution {
    public int findNthDigit(int n) {
        // 从1->+∞开始看n属于哪一位区间
        // i 表示i位数, iRightIndex表示i位数右侧的index
        int iRightIndex = 0;
        int i = 0; // 0就是0位数
        while (iRightIndex < n) {
            i ++;
            iRightIndex += 9 * Math.pow(10, i - 1) * i;
        }
        // n是i位数
        if (i == 0) {
            return 0;
        } else if (i == 1) {
            return n;
        }
        // 拿到n在i位数的范围区间[10^(i-1), 10^i - 1]的index
        for (int k = 1; k < i; k ++) {
            n -= 9 * Math.pow(10, k - 1) * k;
        }
        System.out.println(n);
        System.out.println(i);
        // n 现在就是n在i位范围区间的index+1了
        n--;
        int number = n / i + (int)Math.pow(10, i-1);
        int numberI = n % i;
        return (number+"").charAt(numberI) - '0';
    }
}
```