[剑指 Offer 32 - I. 从上到下打印二叉树](https://leetcode-cn.com/problems/cong-shang-dao-xia-da-yin-er-cha-shu-lcof/)

从上到下打印出二叉树的每个节点，同一层的节点按照从左到右的顺序打印。

思路: 
1. BFS, 用一个queue来把左右的node都按照顺序入队. 按照顺序出队就好了.
2. 把BFS改成递归.... 每层维护一个list, 总的listlist保存着. 从左到右遍历, 然后放在对应层的list里.

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
    public int[] levelOrder(TreeNode root) {
        if (root == null) {
            return new int[]{};
        }
        List<Integer> res = new ArrayList<>();

        // WFS: 
        LinkedList<TreeNode> list = new LinkedList<>();
        list.add(root);
        while (list.size() > 0) {
            // 开始每层便利
            TreeNode item = list.poll();
            // 这一个item处理
            res.add(item.val);
            // item的下一层放进去
            if (item.left != null) {
                list.offer(item.left);
            }
            if (item.right != null) {
                list.offer(item.right);
            }
        }

        return res.stream().mapToInt(o-> o).toArray();

        // DFS: ??????????????????????????????????????????????????/
    }
}
```