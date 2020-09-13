[剑指 Offer 07. 重建二叉树](https://leetcode-cn.com/problems/zhong-jian-er-cha-shu-lcof/)

输入某二叉树的前序遍历和中序遍历的结果，请重建该二叉树。假设输入的前序遍历和中序遍历的结果中都不含重复的数字。

```text
前序遍历 preorder = [3,9,20,15,7]
中序遍历 inorder = [9,3,15,20,7]
返回如下的二叉树：
    3
   / \
  9  20
    /  \
   15   7
```

思路: 递归, 用两个遍历的特点, 开始组装.

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
    public TreeNode buildTree(int[] preorder, int[] inorder) {
        if (preorder == null || preorder.length == 0) {
            return null;
        } else if (preorder.length == 1) {
            return new TreeNode(preorder[0]);
        }

        // 1. 先找到第一个
        TreeNode parent = new TreeNode(preorder[0]);

        // 2. 在中序里找到中节点
        int i = 0;
        for (; i < inorder.length; i++) {
            if (inorder[i] == parent.val) {
                break;
            }
        }

        // 3. 重建 左子树 和 右子树; [0, i - 1], [i+1, inorder.length-1] 分别是左右子树
        parent.left = build(preorder, 1, i, inorder, 0, i - 1);
        parent.right = build(preorder, i + 1, preorder.length - 1, inorder, i + 1, inorder.length - 1);

        return parent;
    }

    public TreeNode build(int[] preorder, int pStart, int pEnd, int[] inorder, int iStart, int iEnd) {
        if (pStart > pEnd || iStart > iEnd) {
            return null;
        } else if (pStart == pEnd) {
            return new TreeNode(preorder[pStart]);
        } else if (pStart < 0 || pEnd > preorder.length - 1 || iStart < 0 || iEnd > inorder.length - 1) {
            return null;
        }

        // 1. 先找到第一个
        TreeNode parent = new TreeNode(preorder[pStart]);
        // 2. 在中序里找到中节点
        int i = iStart;
        for (; i <= iEnd; i++) {
            if (inorder[i] == preorder[pStart]) {
                break;
            }
        }
        int leftLength = i - iStart + 1;
        // 3. 重建 左右 子树
        parent.left = build(preorder, pStart + 1, pStart + leftLength - 1, inorder, iStart,  i - 1);
        parent.right = build(preorder, pStart + leftLength, pEnd , inorder,  i + 1, iEnd );

        return parent;
    }
}
```