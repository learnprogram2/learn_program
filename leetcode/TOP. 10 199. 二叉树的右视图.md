[199. 二叉树的右视图](https://leetcode-cn.com/problems/binary-tree-right-side-view/solution/bfsbian-li-by-wangyk-h0sr/)

给定一棵二叉树，想象自己站在它的右侧，按照从顶部到底部的顺序，返回从右侧所能看到的节点值。
```java
输入: [1,2,3,null,5,null,4]
输出: [1, 3, 4]
解释:
   1            <---
 /   \
2     3         <---
 \     \
  5     4       <---
```

思路: 
1. BFS遍历, 取每层最右侧
2. DFS遍历??