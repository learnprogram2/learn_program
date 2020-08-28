[面试题 17.24. 最大子矩阵](https://leetcode-cn.com/problems/max-submatrix-lcci/)

给定一个正整数和负整数组成的 N × M 矩阵，编写代码找出元素总和最大的子矩阵。

返回一个数组 [r1, c1, r2, c2]，其中 r1, c1 分别代表子矩阵左上角的行号和列号，r2, c2 分别代表右下角的行号和列号。若有多个满足条件的子矩阵，返回任意一个均可。

注意：本题相对书上原题稍作改动

```text
输入:
[
   [-1,0],
   [0,-1]
]
输出: [0,1,0,1]
解释: 输入中标粗的元素即为输出所表示的矩阵
```

思路: 
1. 动态规划: 我还不会~~~~~~~~~~~~~~~~~
2. 暴力破解, 固定左上角, 然后遍历右下角, 超时了.




```java
class Solution {
    
    public int[] getMaxMatrix(int[][] matrix) {

    }







    // // =========================================================
    // // 超时了~~~
    // // =========================================================
    // int maxSum = Integer.MIN_VALUE;
    // int[] res = new int[]{-1, -1, -1, -1};
    // public int[] getMaxMatrix(int[][] matrix) {
    //     // 自制: 暴力破解.
    //     // 固定住一个点, 假设我们遍历的这个点就是最大的子矩阵的左上角
        
    //     for (int i = 0; i < matrix.length; i ++) {
    //         for (int j = 0; j < matrix[0].length; j ++) {
    //             calculateMax(matrix, i, j);
    //         }
    //     }

    //     if (res[0] == -1)  {
    //         return new int[]{0,0,0,0};
    //     }
    //     return res;
    // }

    // // 上面固定了左上角, 现在遍历右下角, 遍历计算max
    // public void calculateMax(int[][] matrix, int i, int j) {

    //     for (int x = i; x < matrix.length; x ++) {
    //         for (int y = j; y < matrix[0].length; y ++) {
    //             int sum = calculate(matrix, i, j, x, y);
    //             if (sum > maxSum) {
    //                 res[0] = i;
    //                 res[1] = j;
    //                 res[2] = x;
    //                 res[3] = y;
    //                 maxSum = sum;
    //             }
    //         }
    //     }
    // }
    // public int calculate(int[][] matrix, int i, int j, int x, int y) {
    //     int sum = 0;
    //     for (int m = i; m <= x; m ++) {
    //         for (int n = j; n <= y; n ++) {
    //             sum += matrix[m][n];
    //         }
    //     }
    //     return sum;
    // }
    
    
    
    
      public int[] getMaxMatrix(int[][] matrix) {
        int maxSum = Integer.MIN_VALUE;
        int[] res = new int[]{-1, -1, -1, -1};
        // 列上的前缀和
        int[][] colPreSum = new int[matrix.length][matrix[0].length];

        // 从左到右列滑动
        for (int j = 0; j < matrix[0].length; j ++) {
            colPreSum[0][j] = matrix[0][j];
            // 统计每列的sum
            for (int i = 1; i < matrix.length; i ++) {
                colPreSum[i][j] = colPreSum[i-1][j] + matrix[i][j];
            }
        }

        // 
        int[] tmpArr = new int[matrix.length];
        for (int i = 0; i < matrix.length; i ++) {
            for (int j = i; j < matrix.length; j ++) {
                // 从i到j行之间合并
                for (int col = 0; col < matrix[0].length; col ++) {
                    tmpArr[col] = i == 0 ? colPreSum[j][col] : colPreSum[j][col] - colPreSum[i-1][col];
                }
                // 求一维数组最大子数组和
            }
        }

    }

    // 最大子矩阵，最大子矩形
    public int[] getMaxMatrix(int[][] matrix) {
        int m = matrix.length;
        int n = matrix[0].length;
        int ansMax = Integer.MIN_VALUE;
        int[] ansArr = new int[4];
        int[][] colPreSum = new int[m][n];  // 列上的前缀和

        for (int j = 0; j < n; j++) {
            colPreSum[0][j] = matrix[0][j];
            for (int i = 1; i < m; i++) {
                colPreSum[i][j] = colPreSum[i-1][j] + matrix[i][j];
            }
        }

        int[] tmpArr = new int[n];
        for (int fromRow = 0; fromRow < m; fromRow++) {
            for (int toRow = fromRow; toRow < m; toRow++) {
                // 第fromRow行到第toRow行进行合并
                for (int col = 0; col < n; col++) {
                    tmpArr[col] = fromRow == 0 ? colPreSum[toRow][col] : colPreSum[toRow][col] - colPreSum[fromRow - 1][col];
                }

                // 求一维数组的最大子数组和
                int[] maxArrayRes = getMaxArray(tmpArr);
                int max = maxArrayRes[0];
                int maxFromCol = maxArrayRes[1];
                int maxToCol = maxArrayRes[2];

                if (max > ansMax) {
                    ansMax = max;
                    ansArr[0] = fromRow;
                    ansArr[1] = maxFromCol;
                    ansArr[2] = toRow;
                    ansArr[3] = maxToCol;
                }
            }
        }

        return ansArr;
    }

}


```