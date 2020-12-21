[剑指 Offer 61. 扑克牌中的顺子](https://leetcode-cn.com/problems/bu-ke-pai-zhong-de-shun-zi-lcof/solution/n-n4fan-wei-nei-de-liang-ge-gui-ze-by-wa-2m7p/)

从扑克牌中随机抽5张牌，判断是不是一个顺子，即这5张牌是不是连续的。2～10为数字本身，A为1，J为11，Q为12，K为13，而大、小王为 0 ，可以看成任意数字。A 不能视为 14。

```java
输入: [1,2,3,4,5]
输出: True
```

看题解: 限制在[n, n+4]范围之内, 两个原则: max-min, 然后是不能有重复. 总数是背景.

