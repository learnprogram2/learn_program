[剑指 Offer 39. 数组中出现次数超过一半的数字](https://leetcode-cn.com/problems/shu-zu-zhong-chu-xian-ci-shu-chao-guo-yi-ban-de-shu-zi-lcof/)

数组中有一个数字出现的次数超过数组长度的一半，请找出这个数字。



思路: 摩尔投票法, 感觉很奇妙.

```java
class Solution {
    public int majorityElement(int[] nums) {
        // 1. 排序, 然后取中间
        // 2. 摩尔投票

        if (nums == null || nums.length == 0) {
            return -1;
        }
        // 假设众数是第一个
        int moreHalf = nums[0];
        int count = 1; // 众数计数
        for (int i = 1; i < nums.length; i ++) {
            if (moreHalf == nums[i]) {
                // 众数出现了
                count ++;
            } else {
                // 另一个数
                count --;
                if (count < 0) {
                    moreHalf = nums[i];
                    count = 1;
                }
            }
        }
        if (count > 0) {
            return moreHalf;
        }
        return -1;
    }
}
```










