[剑指 Offer 29. 顺时针打印矩阵](https://leetcode-cn.com/problems/shun-shi-zhen-da-yin-ju-zhen-lcof/submissions/)

1. 遍历, 不要越界.
2. 适当时候, 退出

```java
class Solution {
    public int[] spiralOrder(int[][] matrix) {
        if (matrix == null || matrix.length == 0) {
            return new int[]{};
        }
        int[] res = new int[matrix.length * matrix[0].length];
        int count = 0;
        int circleNum = (Math.min(matrix.length, matrix[0].length) + 1) / 2;
        for (int i = 0; i < circleNum; i++) {
            // 行: i; 列: [i, matrix[0].length-1-i]
            for (int j = i; j < matrix[0].length - i; j++) {
                res[count++] = matrix[i][j];
            }
            if (res.length == count)
                return res;
            // right: 行[i+1, matrix.length-1-i], 列[matrix[0].length-1-i]
            for (int j = i + 1; j < matrix.length - i; j++) {
                res[count++] = matrix[j][matrix[0].length - 1 - i];
            }
            if (res.length == count)
                return res;
            // bottom: 行[matrix.length-1-i], 列[matrix[0].length-1-i-1, i]
            for (int j = matrix[0].length - 1 - i - 1; j >= i; j--) {
                res[count++] = matrix[matrix.length - 1 - i][j];
            }
            if (res.length == count)
                return res;
            // left: 行[matrix.length-1-i-1, i+1], 列[i]
            for (int j = matrix.length - 1 - i - 1; j >= i + 1; j--) {
                res[count++] = matrix[j][i];
            }
            if (res.length == count)
                return res;
        }
        return res;
    }
}
```




