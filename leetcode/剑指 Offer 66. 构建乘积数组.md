[剑指 Offer 66. 构建乘积数组](https://leetcode-cn.com/problems/gou-jian-cheng-ji-shu-zu-lcof/solution/zuo-you-liang-ge-bian-li-de-cheng-ji-shu-xtr0/)

给定一个数组 A[0,1,…,n-1]，请构建一个数组 B[0,1,…,n-1]，其中 B 中的元素 B[i]=A[0]×A[1]×…×A[i-1]×A[i+1]×…×A[n-1]。不能使用除法。
```java
输入: [1,2,3,4,5]
输出: [120,60,40,30,24]
```

思路: 维护两个左右乘积的数组, 也可以省略一个, 直接用res[]代替.

