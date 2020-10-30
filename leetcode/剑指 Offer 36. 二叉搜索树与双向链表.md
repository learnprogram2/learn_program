[剑指 Offer 36. 二叉搜索树与双向链表](https://leetcode-cn.com/problems/er-cha-sou-suo-shu-yu-shuang-xiang-lian-biao-lcof/)

输入一棵二叉搜索树，将该二叉搜索树转换成一个排序的循环双向链表。要求不能创建任何新的节点，只能调整树中节点指针的指向。

思路: 
1. 中序遍历
2. 操作指针, 注意先遍历再操作指针.


```java
/*
// Definition for a Node.
class Node {
    public int val;
    public Node left;
    public Node right;

    public Node() {}

    public Node(int _val) {
        val = _val;
    }

    public Node(int _val,Node _left,Node _right) {
        val = _val;
        left = _left;
        right = _right;
    }
};
*/
class Solution {
    Node head;
    Node tail;
    public Node treeToDoublyList(Node root) {
        if (root == null) {
            return null;
        }
        head = root;
        while (head.left != null) {
            head = head.left;
        }
        tail = root;
        while (tail.right != null) {
            tail = tail.right;
        }

        leftPart(root.left, root);
        rightPart(root.right, root);
        
        head.left = tail;
        tail.right = head;

        return head;
    }
    public void leftPart(Node root, Node parent) {
        if (root == null) {
            return ;
        }
        // left
        leftPart(root.left, root);
        // right
        rightPart(root.right, root);
        while (root.right != null) {
            root = root.right;
        }
        root.right = parent;
        parent.left = root;
    }
    public void rightPart(Node root, Node parent) {
        if (root == null) {
            return ;
        } 
        // left
        leftPart(root.left, root);
        // right
        rightPart(root.right, root);
        while (root.left != null) {
            root = root.left;
        }
        root.left = parent;
        parent.right = root;
    }
}
```







