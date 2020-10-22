[剑指 Offer 32 - III. 从上到下打印二叉树 III](https://leetcode-cn.com/problems/cong-shang-dao-xia-da-yin-er-cha-shu-iii-lcof/submissions/)

请实现一个函数按照之字形顺序打印二叉树，即第一行按照从左到右的顺序打印，第二层按照从右到左的顺序打印，第三行再按照从左到右的顺序打印，其他行以此类推。

思路: 和Ⅱ一样, 分层, 但是每层放一个dequeue, 根据要求的奇偶性, 左右放, 就能保证顺序了

```java
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
            res.add(new LinkedList<>());
        }
        // 1. 把自己加上对应的层里. 根据层的奇偶选择从左还是从右放
        if (level % 2 != 0) {
            res.get(level - 1).add(root.val);
        } else {
            ((LinkedList) res.get(level - 1)).offerFirst(root.val);
        }
        // 2. 先放左, 再放右. 每层都是, 保证了每层的从左到右顺序.
        levelOrder(res, root.left, level + 1);
        levelOrder(res, root.right, level + 1);

        return res;
    }
}
```
