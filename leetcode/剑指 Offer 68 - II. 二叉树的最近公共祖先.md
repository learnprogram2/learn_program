[剑指 Offer 68 - II. 二叉树的最近公共祖先](https://leetcode-cn.com/problems/er-cha-shu-de-zui-jin-gong-gong-zu-xian-lcof/solution/di-gui-xun-zhao-lu-jing-pan-duan-gong-go-8s2o/)

给定一个二叉树, 找到该树中两个指定节点的最近公共祖先。

百度百科中最近公共祖先的定义为：“对于有根树 T 的两个结点 p、q，最近公共祖先表示为一个结点 x，满足 x 是 p、q 的祖先且 x 的深度尽可能大（一个节点也可以是它自己的祖先）。”

```java
输入: root = [3,5,1,6,2,0,8,null,null,7,4], p = 5, q = 1
输出: 3
解释: 节点 5 和节点 1 的最近公共祖先是节点 3。
```

思路: 
相比上一题, 这里不是二叉搜索树了, 所以两者的路径不知道了, 不能找最近的能把两个分开的了. 所以要把这条路径找出来.

-- 重要: 二叉搜索树就是相当于已知路径了!


