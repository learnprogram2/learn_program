[剑指 Offer 04. 二维数组中的查找](https://leetcode-cn.com/problems/er-wei-shu-zu-zhong-de-cha-zhao-lcof/)

在一个 n * m 的二维数组中，每一行都按照从左到右递增的顺序排序，每一列都按照从上到下递增的顺序排序。请完成一个函数，输入这样的一个二维数组和一个整数，判断数组中是否含有该整数。

```text
示例:
[
  [1,   4,  7, 11, 15],
  [2,   5,  8, 12, 19],
  [3,   6,  9, 16, 22],
  [10, 13, 14, 17, 24],
  [18, 21, 23, 26, 30]
]
```

思路: 可以利用特点, 就从中间遍历就好了

```java
class Solution {
    public boolean findNumberIn2DArray(int[][] matrix, int target) {
        return ifHas(matrix, matrix.length-1, 0, target);
    }

    public boolean ifHas(int[][] matrix, int i, int j, int target) {
        if (i < 0 || j >= matrix[0].length) {
            return false;
        }
        // 对比
        int curr = matrix[i][j];
        if (curr == target) {
            return true;
        } else if(curr > target) {
            return ifHas(matrix, i-1, j, target);
        } else {
            return ifHas(matrix, i, j + 1, target);
        }
    }
}
```







