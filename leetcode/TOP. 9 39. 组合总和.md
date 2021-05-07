[39. 组合总和](https://leetcode-cn.com/problems/combination-sum/solution/dfsjian-zhi-hui-su-by-wangyk-2mhf/)

给定一个无重复元素的数组 candidates 和一个目标数 target ，找出 candidates 中所有可以使数字和为 target 的组合。

candidates 中的数字可以无限制重复被选取。

说明：
所有数字（包括 target）都是正整数。
解集不能包含重复的组合。 
```java
输入：candidates = [2,3,6,7], target = 7,
所求解集为：
[
  [7],
  [2,2,3]
]
```

思路:
1. dfs遍历可能树 + 回溯



