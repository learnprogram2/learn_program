[剑指 Offer 21. 调整数组顺序使奇数位于偶数前面](https://leetcode-cn.com/problems/diao-zheng-shu-zu-shun-xu-shi-qi-shu-wei-yu-ou-shu-qian-mian-lcof/)

输入一个整数数组，实现一个函数来调整该数组中数字的顺序，使得所有奇数位于数组的前半部分，所有偶数位于数组的后半部分。

```text
输入：nums = [1,2,3,4]
输出：[1,3,2,4] 
注：[3,1,2,4] 也是正确的答案之一。
```

思路: 

        // 前后双指针 往中间挤;
        // 还可以快慢双指针, 从左侧出发, slow记录分界点, fast寻找右侧奇数.

```java
class Solution {
    public int[] exchange(int[] nums) {
        // 前后双指针 往中间挤;
        // 还可以快慢双指针, 从左侧出发, slow记录分界点, fast寻找右侧奇数.

        if (nums == null) {
            return new int[]{};
        }

        int left = 0, right = nums.length - 1;
        while (left < right) {
            if (!isOdd(nums[left])) {
                // 如果左边是偶数, 就挪到右边
                change (nums, left, right);
                right --;
            } else {
                // 左边是奇数, 接着往下走
                left ++;
            }
        }

        return nums;        
    }

    private boolean isOdd(int number) {
        return number % 2 == 0 ? false : true;
    }

    private void change(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }

}
```






