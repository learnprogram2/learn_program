[剑指 Offer 53 - I. 在排序数组中查找数字 I](https://leetcode-cn.com/problems/zai-pai-xu-shu-zu-zhong-cha-zhao-shu-zi-lcof/)

统计一个数字在排序数组中出现的次数。
```
输入: nums = [5,7,7,8,8,10], target = 8
输出: 2
```

思路: 二分法找到就好了, 前后前后找. 复杂度是ln(n)


```java
class Solution {
    public int search(int[] nums, int target) {
        if (nums == null || nums.length == 0) {
            return 0;
        }
        
        // 二分法查到, 然后前后算个指针计算
        int left = 0, right = nums.length-1;
        while (left < right) {
            int med = (right - left) / 2 + left;
            if (nums[med] > target) {
                right = med - 1;
            } else if (nums[med] < target){
                left = med + 1;
            } else {
                left = med;
                break;
            }
        }
        if (nums[left] != target) {
            return 0;
        }
        int count = 1;
        for (int i = left + 1; i < nums.length; i ++) {
            if (nums[i] == target) {
                count ++;
            } else {
                break;
            }
        }
        for (int i = left - 1; i >= 0; i --) {
            if (nums[i] == target) {
                count ++;
            } else {
                break;
            }
        }
        return count;

    }
}
```








