[剑指 Offer 20. 表示数值的字符串](https://leetcode-cn.com/problems/biao-shi-shu-zhi-de-zi-fu-chuan-lcof/)

请实现一个函数用来判断字符串是否表示数值（包括整数和小数）。例如，字符串"+100"、"5e2"、"-123"、"3.1416"、"-1E-16"、"0123"都表示数值，但"12e"、"1a3.14"、"1.2.3"、"+-5"及"12e+5.4"都不是。

思路: 

遍历判断规则: e前后必须是两个完整数, 不能有多个逗号, +-号只能出现在第一位

```java
class Solution {
    public boolean isNumber(String s) {

        // 非空校验
        if (s == null || s.equals("")){
            return false;
        }
        boolean isNumber = false, isDot = false, isE = false;
        char[] arr = s.trim().toCharArray();
        for (int i = 0; i < arr.length; i ++) {
            if (arr[i] == '.'){
                // 小数点, 前面不能有小数点和E, 前面必须是一个正数
                if (isDot || isE)
                    return false;
                isDot = true;
            } else if (arr[i] == 'e' || arr[i] == 'E') {
                // E, 前面不许有E, 前面必须是一个数(可以是小数)
                if ( isE || !isNumber)
                    return false;
                isE = true;
                // * 重置判断, 因为e后面也必须是一个完整的数(不能再包含e)
                isNumber = false;
            } else if (arr[i] == '-' || arr[i] == '+') {
                // 正负号, 只能出现在第一个(或者e后面第一个)
                if (i != 0 && arr[i-1] != 'e' && arr[i-1] != 'E')
                    return false;
            } else if (arr[i] >= '0' && arr[i] <= '9'){
                // 只要满足上面的情况, 都可以看成一个数, (e前后应该是两个完整的数)
                isNumber = true;
            } else {
                // 除了0之外的其他字符
                return false;
            }
        }
        return isNumber;
    }
}
```

