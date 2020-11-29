[剑指 Offer 53 - II. 0～n-1中缺失的数字](https://leetcode-cn.com/problems/que-shi-de-shu-zi-lcof/solution/bian-li-by-wangyk-5/)

一个长度为n-1的递增排序数组中的所有数字都是唯一的，并且每个数字都在范围0～n-1之内。在范围0～n-1内的n个数字中有且只有一个数字不在该数组中，请找出这个数字。
```text
输入: [0,1,3]
输出: 2
```

思路: 下面两种方法:
注意: 要注意缺少的是0还是n, 这两个边界要思考清楚.

**重要**: 二分法也可以应用啊!!!!

```java
    public int missingNumber(int[] nums) {
        // 1. 求和 然后用公式(n*(n-1)/2)减, 就得到miss的
        // 2. 遍历: 既然是递增的, 就马上找到.
        if (nums == null || nums.length == 0) {
            return -1;
        }
        int i = 0;
        for (; i < nums.length; i ++) {
            if (nums[i] != i) {
                return i;
            }
        }
        return i == nums.length ? nums.length : -1;
    }
```