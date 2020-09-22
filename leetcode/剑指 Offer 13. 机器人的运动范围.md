[剑指 Offer 13. 机器人的运动范围](https://leetcode-cn.com/problems/ji-qi-ren-de-yun-dong-fan-wei-lcof/)

地上有一个m行n列的方格，从坐标 [0,0] 到坐标 [m-1,n-1] 。一个机器人从坐标 [0, 0] 的格子开始移动，它每次可以向左、右、上、下移动一格（不能移动到方格外），也不能进入行坐标和列坐标的数位之和大于k的格子。例如，当k为18时，机器人能够进入方格 [35, 37] ，因为3+5+3+7=18。但它不能进入方格 [35, 38]，因为3+5+3+8=19。请问该机器人能够到达多少个格子？

```java
输入：m = 2, n = 3, k = 1
输出：3
```

思路: 

1. DFS: 深度遍历

2. BFS: 广度遍历: 还没想出该怎么做. 感觉BFS不合适啊, 要维护每一层. 然后呢???

```java
    public int getBitsSum(int m, int n) {
        int sum = 0;
        while (m > 0) {
            sum += m % 10;
            m = m / 10;
        }
        while (n > 0) {
            sum += n % 10;
            n = n / 10;
        }
        return sum;
    }

    // DFS
    // int[][] arr;
    // public int movingCount(int m, int n, int k) {
    //     arr = new int[m][n];
    //     // 从[0,0]开始DFS
    //     int sum = move(m, n, 0, 0, k);
    //     return sum;
    // }
    // public int move(int m, int n, int i, int j, int k) {
    //     // range
    //     if (i < 0 || i >= m || j < 0 || j >= n) {
    //         return 0;
    //     }
    //     if (arr[i][j] != 0) {
    //         return 0;
    //     }

    //     // 判断当前i,j符不符合要求
    //     int bitesSum = getBitsSum(i, j);
    //     if (bitesSum > k) {
    //         return 0;
    //     }
    //     // 四处看看
    //     arr[i][j] = 1;
    //     return 1 + move(m, n, i-1, j, k) + move(m, n, i, j-1, k) + move(m, n, i+1, j, k) + move(m, n, i, j+1, k);
    // }
```







