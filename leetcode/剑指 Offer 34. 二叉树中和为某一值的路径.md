[剑指 Offer 34. 二叉树中和为某一值的路径](https://leetcode-cn.com/problems/er-cha-shu-zhong-he-wei-mou-yi-zhi-de-lu-jing-lcof/)

输入一棵二叉树和一个整数，打印出二叉树中节点值的和为输入整数的所有路径。从树的根节点开始往下一直到叶节点所经过的节点形成一条路径。

```text
给定如下二叉树，以及目标和 sum = 22，
              5
             / \
            4   8
           /   / \
          11  13  4
         /  \    / \
        7    2  5   1
```

思路: 注意 要统计分数和路径, 然后要维持着这两个, 一直遍历.
第二个,要注意一直到叶子节点.

```java/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode(int x) { val = x; }
 * }
 */
class Solution {
    List<List<Integer>> res = new ArrayList<>();
    public List<List<Integer>> pathSum(TreeNode root, int sum) {
        // 和为 sum的路径. sum 固定, 要记录路径上的sum
        List<Integer> path = new ArrayList<>();
        pathSum (path, root, 0, sum);
        return res;
    }
    public void pathSum(List<Integer> path, TreeNode node, int beforeSum, int sum) {
        if (node == null) {
            return;
        }
        List<Integer> a = new ArrayList<>();
        a.addAll(path);
        a.add(node.val);

        if (beforeSum + node.val == sum && node.left == null && node.right == null) { 
            res.add(a);
            return ;
        } 

        beforeSum += node.val;
        pathSum(a, node.left, beforeSum, sum);
        pathSum(a, node.right, beforeSum, sum);
    }
}
```