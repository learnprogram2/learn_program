[剑指 Offer 11. 旋转数组的最小数字](https://leetcode-cn.com/problems/xuan-zhuan-shu-zu-de-zui-xiao-shu-zi-lcof/)

把一个数组最开始的若干个元素搬到数组的末尾，我们称之为数组的旋转。输入一个递增排序的数组的一个旋转，输出旋转数组的最小元素。例如，数组 [3,4,5,1,2] 为 [1,2,3,4,5] 的一个旋转，该数组的最小值为1。  

```
输入：[3,4,5,1,2]
输出：1
```

思路: 思路很简单: 二分法最好. 但是中间指针左右加减, 要调试. 很难的哦~


```java
class Solution {
    public int minArray(int[] numbers) {
        if (numbers.length == 0) {
            return -1;
        } else if (numbers.length == 1) {
            return numbers[0];
        } else if (numbers[numbers.length - 1] > numbers[0]) {
            return numbers[0];
        }

        // 這個裏面的判斷邏輯: 很重要, 要調試. ************************
        int si = 0, ei = numbers.length - 1;
        while (si < ei) {
            int midi = si + (ei - si) / 2;
            if (numbers[midi] > numbers[ei]) {
                si = midi + 1;
            } else if (numbers[midi] < numbers[ei]) {
                ei = midi;
            } else {
                ei--;
            }
        }
        // ********************************************************
        return numbers[si];
    }
}
```