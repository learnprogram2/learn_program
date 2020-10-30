[剑指 Offer 37. 序列化二叉树](https://leetcode-cn.com/problems/xu-lie-hua-er-cha-shu-lcof/)

请实现两个函数，分别用来序列化和反序列化二叉树。

```text
你可以将以下二叉树：
    1
   / \
  2   3
     / \
    4   5

序列化为 "[1,2,3,null,null,4,5]"
```

注意: 没有说序列化什么样式的, 怎样都可以.


```java
public class Codec {

    // 我这个是层级的, 但是题目要求的序列化并没有要求, 层级或者什么, 只需要实现序列化和反序列化.
    
    public int countDepth(TreeNode root, int currentDepth) {
        if (root == null) {
            return currentDepth - 1;
        }
        return Math.max(
                countDepth(root.left, currentDepth + 1), countDepth(root.right, currentDepth + 1)
        );
    }

    // 层序遍历.
    public String serialize(TreeNode root) {
        if (root == null) {
            return "[]";
        }
        int depth = countDepth(root, 1);
        // 先填上第一层
        List<List<TreeNode>> res = new ArrayList<>();
        res.add(Arrays.asList(root));
        // 根据上一层, 遍历之后的
        for (int i = 1; i < depth; i++) {
            List<TreeNode> parent = res.get(i - 1);
            List<TreeNode> current = new ArrayList<>();
            for (TreeNode node : parent) {
                if (node == null) {
                    current.add(null);
                    current.add(null);
                } else {
                    current.add(node.left);
                    current.add(node.right);
                }
            }
            res.add(current);
        }
        // 把list 转成String
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < depth; i++) {
            for (TreeNode node : res.get(i)) {
                sb.append(node == null ? "null," : node.val + ",");
            }
        }
        sb.replace(sb.length() - 1, sb.length(), "]");
        return sb.toString();
    }


    // Decodes your encoded data to tree.
    public TreeNode deserialize(String data) {
        if (data.length() == 2) {
            return null;
        }
        String[] dataArr = data.substring(1, data.length() - 1).split(",");

        // 把arr的int, 都生成Node放在list;
        List<TreeNode> nodeList = new ArrayList<>(dataArr.length);
        for (int i = 0; i < dataArr.length; i++) {
            if ("null".equals(dataArr[i])) {
                nodeList.add(null);
            } else {
                nodeList.add(new TreeNode(Integer.parseInt(dataArr[i])));
            }
        }

        // 操作list的Node的left和right
        int currDepth = 0;
        double currDepthLeftIndex = 0;
        for (; currDepthLeftIndex < dataArr.length; currDepthLeftIndex += Math.pow(2, currDepth), currDepth++) {
            // current 是 currDepth 层的左index. 在 dataArr 里面;
            double nextDepthLeftIndex = currDepthLeftIndex + Math.pow(2, currDepth);
            if (nextDepthLeftIndex >= dataArr.length) {
                break;
            }
            // 开始遍历[currDepthLeftIndex, nextDepthLeftIndex)之间的Node, 把它们指向对应的下一层Node的Index;
            for (int i = (int) currDepthLeftIndex; i < nextDepthLeftIndex; i++) {
                if (nodeList.get(i) != null) {
                    // 把node的指针指向下一层
                    int nextIndex = (int) (nextDepthLeftIndex + 2 * (i - currDepthLeftIndex));
                    nodeList.get(i).left = nodeList.get(nextIndex);
                    nodeList.get(i).right = nodeList.get(nextIndex + 1);
                }
            }
        }

        return nodeList.get(0);
    }


}
```