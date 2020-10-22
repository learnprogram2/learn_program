[剑指 Offer 32 - II. 从上到下打印二叉树 II](https://leetcode-cn.com/problems/cong-shang-dao-xia-da-yin-er-cha-shu-ii-lcof/)

从上到下按层打印二叉树，同一层的节点按从左到右的顺序打印，每一层打印到一行。

思路: 
deep的时候从左到右保证顺序, 结果list分层, 每个node都知道自己放的位置.

```java
/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode(int x) { val = x; }
 * }
 */
class Solution {
    public List<List<Integer>> levelOrder(TreeNode root) {
        if (root == null) {
            return new ArrayList<>();
        }
        // 每一层打印到一行.
        List<List<Integer>> res = new ArrayList<>();
        levelOrder(res, root, 1);
        return res;
    }
    public List<List<Integer>> levelOrder(List<List<Integer>> res, TreeNode root, int level) {
        if (root == null) {
            return res;
        }
        // 先看有没有这层的地方;
        if (res.size() < level) {
            res.add(new ArrayList<>());
        }
        // 1. 把自己加上对应的层里
        res.get(level - 1).add(root.val);
        // 2. 先放左, 再放右. 每层都是, 保证了每层的从左到右顺序.
        levelOrder(res, root.left, level + 1);
        levelOrder(res, root.right, level + 1);
        
        return res;
    }
}
```