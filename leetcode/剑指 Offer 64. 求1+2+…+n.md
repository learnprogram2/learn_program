[剑指 Offer 64. 求1+2+…+n](https://leetcode-cn.com/problems/qiu-12n-lcof/)

求 1+2+...+n ，要求不能使用乘除法、for、while、if、else、switch、case等关键字及条件判断语句（A?B:C）。
```java
输入: n = 3
输出: 6
```


```java
class Solution {
    int sum = 0;
    public int sumNums(int n) {
        // 递归? 这就是用了for, 违规了.

       // 用递归, 怎么终止: 用 || 和 && 阻断
        boolean noUse = n > 1 && sumNums(n - 1) > 0;
        sum += n;
        return sum;
    }
}
```


