[剑指 Offer 19. 正则表达式匹配](https://leetcode-cn.com/problems/zheng-ze-biao-da-shi-pi-pei-lcof/)

请实现一个函数用来匹配包含'. '和'*'的正则表达式。模式中的字符'.'表示任意一个字符，而'*'表示它前面的字符可以出现任意次（含0次）。在本题中，匹配是指字符串的所有字符匹配整个模式。例如，字符串"aaa"与模式"a.a"和"ab*ac*a"匹配，但与"aa.a"和"ab*a"均不匹配。

```text
s = "aa"
p = "a"
输出: false
解释: "a" 无法匹配 "aa" 整个字符串。
```



```java
  public boolean isMatch(String s, String p) {
        // 遍历p去匹配:
        int pi = 0;
        int i = 0;

        for (; i < p.length(); i++) {
            if (pi >= s.length())
                break;

            // 拿着 i 位字符去匹配
            char ic = p.charAt(i);

            if (ic == '.')
                // 匹配一个任意字符, pi ++
                pi++;
            else if (ic == '*') {
                if (pi == 0)
                    // 开头来了一个 *
                    return false;
                // 匹配 n(0, 1, 2...) 个 s 在 pi-1 位置上的字符
                while (pi < p.length() && s.charAt(pi) == s.charAt(pi - 1))
                    pi++;
            } else if (s.charAt(pi) != ic)
                // 匹配 pic 和 ic
                return false;
            else
                pi++;
        }

        if (pi == s.length() && i == p.length())
            return true;

        return false;
    }

    public boolean isMatch2(String s, String p) {
        // 遍历p去匹配 FIXME: 应该从后往前遍历:
        int pi = p.length();
        int i = s.length();

        for (; i > 0; i++) {
            if (pi >= s.length())
                break;

            // 拿着 i 位字符去匹配
            char ic = p.charAt(i);

            if (ic == '.')
                // 匹配一个任意字符, pi ++
                pi++;
            else if (ic == '*') {
                if (pi == 0)
                    // 开头来了一个 *
                    return false;
                // 匹配 n(0, 1, 2...) 个 s 在 pi-1 位置上的字符
                while (pi < p.length() && s.charAt(pi) == s.charAt(pi - 1))
                    pi++;
            } else if (s.charAt(pi) != ic)
                // 匹配 pic 和 ic
                return false;
            else
                pi++;
        }

        if (pi == s.length() && i == p.length())
            return true;

        return false;
    }

    public static void main(String[] args) {
        Solution solution = new Solution();
        System.out.println(solution.isMatch2("aab", "c*a*b"));
    }
```    