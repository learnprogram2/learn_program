https://leetcode-cn.com/problems/er-cha-sou-suo-shu-de-hou-xu-bian-li-xu-lie-lcof/

输入一个整数数组，判断该数组是不是某二叉搜索树的后序遍历结果。如果是则返回 true，否则返回 false。假设输入的数组的任意两个数字都互不相同。

```text
     5
    / \
   2   6
  / \
 1   3
```



思路: 迭代判断, 可以看成BFS.



```java
class Solution {
    public boolean verifyPostorder(int[] postorder) {
        if (postorder == null || postorder.length < 2) {
            return true;
        }
        // 拿出 最后一个当作root
        int root = postorder[postorder.length - 1];
        // 寻找中间节点; gap做第一个 left
        int gap = -1;
        for (int i = 0; i < postorder.length - 1; i++) {
            if (postorder[i] > root) {
                gap = i;
                break;
            }
        }
        // 校验, left 和 right 两个部分都必须符合二叉搜索树
        if (gap == -1) {
            // 都比root小, 迭代判断
            return verifyPostorder(Arrays.copyOfRange(postorder, 0, postorder.length - 1));
        }
        // 如果有gap, 那么判断right部分
        for (int i = gap + 1; i < postorder.length - 1; i++) {
            if (root >= postorder[i]) {
                return false;
            }
        }
        // 迭代校验
        return verifyPostorder(Arrays.copyOfRange(postorder, 0, gap))
                && verifyPostorder(Arrays.copyOfRange(postorder, gap, postorder.length - 1));
    }
}
````