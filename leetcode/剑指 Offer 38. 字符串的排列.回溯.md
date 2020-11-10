[剑指 Offer 38. 字符串的排列](https://leetcode-cn.com/problems/zi-fu-chuan-de-pai-lie-lcof/)

输入一个字符串，打印出该字符串中字符的所有排列。
你可以以任意顺序返回这个字符串数组，但里面不能有重复元素。

```text
输入：s = "abc"
输出：["abc","acb","bac","bca","cab","cba"]
```

思路: 回溯算法. 这个题s会有重复的char, 所以不能用我自己的办法.


```java
class Solution {
    Set<String> res = new HashSet<>();

    public String[] permutation(String s) {
        // 考虑到s里面重复的字符, 应该进行交换, 
        // 遍历s, 每个位置都和后面每一个char交换. 这样可以保证s里面有重复字符也会把所有可能性都放到str里. 而不是对比char有没有在str里.

        char[] arr = s.toCharArray();

        dfs(arr, 0);

        String[] strArr = new String[res.size()];
        int i = 0;
        for (String str : res) {
            strArr[i ++] = str;
        }
        return strArr;
    }
    public void dfs(char[] arr, int index) {
        if (index == arr.length) {
            res.add(new String(arr));
            return;
        }
        // 把arr中index位置的char, 和后面所有的都交换. 最终得到的就是全量集合. arr的全排列
        // i 从 index开始, index就是一种可能, 然后和后面交换.
       
        for (int i = index; i < arr.length; i ++) {
            // * 在这里, 如果index和i的char相同, 其实可以不用去dfs, 跳过, 这个就是剪枝, 但这个代价太大了.

            // index 和 i 交换
            swap(arr, index, i);
            dfs(arr, index + 1);
            // 再换回来, arr再把i和其他j交换.
            swap(arr, i, index);
        }
    }
    public void swap(char[] arr, int i, int j) {
        char temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }


    ///////////////////////////////////////////////////////////////////下面是错误想法
    public String[] permutation2(String s) {
        char[] arr = s.toCharArray();
        // 想不出来该怎么迭代, 怎么记住自己的迭代顺序?
        // 看了两个题解, 记住自己的迭代顺序这个就是遍历/维持hash记录, 然后每往后一位, 还要回溯, 利用已经遍历的
        // 做了第一遍: 做法是遍历arr里面str没有的, 然后一个一个位置放进str里.  这个是错的, 只能应对不会出现重复字符的.

        addAll(arr, "");

        String[] strArr = new String[res.size()];
        int i = 0;
        for (String str : res) {
            strArr[i ++] = str;
        }
        return strArr;
    }

    public void addAll(char[] arr, String str) {
        // 从Arr中找到left没有的, 然后dfs.
        boolean hasMore = false;
        for (int i = 0; i < arr.length; i ++ ) {
            if (str.contains(arr[i] + "") ) {
                continue;
            }
            hasMore = true;
            addAll(arr, str + arr[i]);
            // 这个时候str跳出了前面的循环, 回来了, 这个叫回溯
        }
        // 如果arr中都在left里面了
        if (!hasMore) {
            res.add(str);
        }
    }

}
```