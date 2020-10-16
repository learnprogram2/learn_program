[剑指 Offer 28. 对称的二叉树](https://leetcode-cn.com/problems/dui-cheng-de-er-cha-shu-lcof/)

请实现一个函数，用来判断一棵二叉树是不是对称的。如果一棵二叉树和它的镜像一样，那么它是对称的。

层级遍历, 左和右判断

```java
class Solution {
	public boolean isSymmetric(TreeNode root) {
        if (root == null || (root.left == root.right)) {
            return true;
        }

        // 左右两个子树		
		return isSymmetric(root.left,root.right);
    }
	
	public boolean isSymmetric(TreeNode left, TreeNode right) {
		if (left == right) {
			return true;
		} else if (left == null || right == null) {
			return false;
		}
		// 判断本层
		if (left.val != right.val) {
			return false;
		}
		// 判断子层
		return isSymmetric(left.left, right.right)
				&& isSymmetric(left.right, right.left);		
	}
}
```












