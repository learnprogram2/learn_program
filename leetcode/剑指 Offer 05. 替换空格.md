[剑指 Offer 05. 替换空格](https://leetcode-cn.com/problems/ti-huan-kong-ge-lcof/solution/)

请实现一个函数，把字符串 s 中的每个空格替换成"%20"。

```text
输入：s = "We are happy."
输出："We%20are%20happy."
```
 
思路: 
1. 使用stringBuilder
2. 创建一个3倍的byte[], 遍历一遍, new String(array, 0, size)

