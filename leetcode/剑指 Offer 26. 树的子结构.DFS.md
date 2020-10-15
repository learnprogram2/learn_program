[剑指 Offer 26. 树的子结构](https://leetcode-cn.com/problems/shu-de-zi-jie-gou-lcof/)

输入两棵二叉树A和B，判断B是不是A的子结构。(约定空树不是任意一个树的子结构);
B是A的子结构， 即 A中有出现和B相同的结构和节点值。

思路: DFS递归判断就好了.

错误点: A要包含B, 而不是A的某个子树和B完全一样. 包含意味着不是子树也可以.

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
    public boolean isSubStructure(TreeNode A, TreeNode B) {
        // DFS 吧, 比较好理解, 感觉不用维护太多

        // 先判断一下
        if (A == null || B == null) {
            return false;
        }

        if (A.val == B.val) {
            // 看看有没有可能相同
            if (isContain(A, B)) {
                return true;
            }
        } 
        
        // 往A的子树看看吧
        return isSubStructure(A.left, B) || isSubStructure(A.right, B);
    }

    public boolean isContain(TreeNode A, TreeNode B) {
        // DFS 判断两棵树是不是完全相同 
        // 错! 要看A要不要包含B.
        if (B == null) {
            return true;
        }
        if (A == null) {
            return false;
        }
        
        if (A.val != B.val) {
            return false;
        }

        // literate, 判断子树
        if (isContain(A.left, B.left) && isContain(A.right, B.right)) {
            return true;
        }

        return false;
    }
}
```