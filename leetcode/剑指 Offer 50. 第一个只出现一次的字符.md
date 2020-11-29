[剑指 Offer 50. 第一个只出现一次的字符](https://leetcode-cn.com/problems/di-yi-ge-zhi-chu-xian-yi-ci-de-zi-fu-lcof/)

在字符串 s 中找出第一个只出现一次的字符。如果没有，返回一个单空格。 s 只包含小写字母。

```
s = "abaccdeff"
返回 "b"
```

思路: 下面写了.


```java
class Solution {
    public char firstUniqChar(String s) {
        // hash 记录出现次数, 就可以了.
        // 看到一位同学写的, 可以从首位检查index, index相同的是第一次出现的. 但这种就是O(n^2)了.

        for (int i = 0; i < s.length(); i ++) {
            // 开始校验:
            if (s.indexOf(s.charAt(i)) == s.lastIndexOf(s.charAt(i))) {
                return s.charAt(i);
            }
        }

        return ' ';
    }
}
```