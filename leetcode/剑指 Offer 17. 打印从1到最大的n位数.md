[剑指 Offer 17. 打印从1到最大的n位数](https://leetcode-cn.com/problems/da-yin-cong-1dao-zui-da-de-nwei-shu-lcof/)

输入数字 n，按顺序打印出从 1 到最大的 n 位十进制数。比如输入 3，则打印出 1、2、3 一直到最大的 3 位数 999。

```text
输入: n = 1
输出: [1,2,3,4,5,6,7,8,9]
```

思路:
1. 简单遍历. 
2. 考虑大数, 要用string, 然后拆分, 归并运算.

```java
class Solution {
    public int[] printNumbers(int n) {
        // if (n == 1) {
        //     return new int[]{1,2,3,4,5,6,7,8,9};
        // }
        // List<Integer> res = new ArrayList<>();
        // for (int i = 1; i < n; i ++) {
        //     res.addAll(Arrays.asList(printNumbers(n - 1)));
        // }

        // // 制作n位数
        // for (int item : printNumbers(n - 1)) {
        //     res.add(10en + item);
        // }
        // for (int i = 1; i <= 9; i ++) {
        //     res.add(10en);
        // }
        
        // return res.toArray();
        int[] res = new int[(int) Math.pow(10, n) - 1];
        for (int i = 1; i < Math.pow(10, n); i ++) {
            res[i - 1] = i;
        }
        return res;
    }
}
```








