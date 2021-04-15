[56. 合并区间](https://leetcode-cn.com/problems/merge-intervals/solution/xian-pai-xu-hou-bian-li-by-wangyk-8mkz/)
以数组 intervals 表示若干个区间的集合，其中单个区间为 intervals[i] = [starti, endi] 。请你合并所有重叠的区间，并返回一个不重叠的区间数组，该数组需恰好覆盖输入中的所有区间。
```java
输入：intervals = [[1,3],[2,6],[8,10],[15,18]]
输出：[[1,6],[8,10],[15,18]]
解释：区间 [1,3] 和 [2,6] 重叠, 将它们合并为 [1,6].
```

题目刚开始没读懂, 意思是: 把所有能合并的区间合并一下.

思路: 先排序后合并.