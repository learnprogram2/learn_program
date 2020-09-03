[剑指 Offer 03. 数组中重复的数字](https://leetcode-cn.com/problems/shu-zu-zhong-zhong-fu-de-shu-zi-lcof/)

找出数组中重复的数字。

在一个长度为 n 的数组 nums 里的所有数字都在 0～n-1 的范围内。数组中某些数字是重复的，但不知道有几个数字重复了，也不知道每个数字重复了几次。请找出数组中任意一个重复的数字。

```java
输入：[2, 3, 1, 0, 2, 5, 3]
输出：2 或 3 
```

思路: 
1. 可以遍历, 用set判断
2. 可以原地置换: 如下. 空间会少一些.

```java
class Solution {
    public int findRepeatNumber(int[] nums) {
        // 归位, 原地置换

        for (int i = 0; i < nums.length; i ++) {
            int now = nums[i];
            if (now != i) {
                while(true) {
                    // 把now放回nums[now]上面; 如果nums[now]上面就是now, 说明now重复了.
                    int temp = nums[now];
                    if (temp == now)
                        return temp;
                    nums[now] = now;
                    now = temp;
                    if (temp == i)
                        break;
                }
            }
            nums[i] = i;            
        }

        return -1;
    }
}
```