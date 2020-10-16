[剑指 Offer 27. 二叉树的镜像](https://leetcode-cn.com/problems/er-cha-shu-de-jing-xiang-lcof/)

请完成一个函数，输入一个二叉树，该函数输出它的镜像。

```text
     4
   /   \
  2     7
 / \   / \
1   3 6   9
镜像输出：

     4
   /   \
  7     2
 / \   / \
9   6 3   1
```


```java
class Solution {
    public TreeNode mirrorTree(TreeNode root) {
        // 交换左右子树
        if (root == null || (root.left == null && root.right == null)) {
            return root;
        }
        //
        TreeNode temp = root.left;
        root.left = root.right;
        root.right = temp;
        // 左右都递归一下
        mirrorTree(root.left);
        mirrorTree(root.right);
        return root;        
    }
}
```








