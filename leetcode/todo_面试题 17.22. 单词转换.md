[面试题 17.22. 单词转换](https://leetcode-cn.com/problems/word-transformer-lcci/)

给定字典中的两个词，长度相等。写一个方法，把一个词转换成另一个词， 但是一次只能改变一个字符。每一步得到的新词都必须能在字典中找到。

编写一个程序，返回一个可能的转换序列。如有多个可能的转换序列，你可以返回任何一个。

```java
输入:
beginWord = "hit",
endWord = "cog",
wordList = ["hot","dot","dog","lot","log","cog"]

输出:
["hit","hot","dot","lot","log","cog"]
```
思路: 深度迭代DFS. 但我的不行, 不能解决死循环问题. 我不知道该怎么做了. 


```java
    //    public List<String> findLadders(String beginWord, String endWord, List<String> wordList) {
    //         //  end 要在list里面
    //         if (!wordList.contains(endWord))
    //             return new ArrayList<>();
    //         // 迭代去查.
    //         List<String> res = new ArrayList<String>();
    //         res.add(beginWord);

    //         List<String> finalRes = find(res, wordList, endWord);

    //         if (finalRes == null) {
    //             return new ArrayList<>();
    //         }
    //         return res;
    //     }

        // public List<String> find(List<String> res, List<String> wordList, String endWord) {
        //     // 拿最后一个word, 看看和endWord是否只差一个, 如果是就返回了
        //     if (onlyOne(res.get(res.size() - 1), endWord)) {
        //         res.add(endWord);
        //         return res;
        //     }

        //     for (int i = 0; i < wordList.size(); i++) {
        //         // 包含了就跳过, 避免死循环
        //         if (res.contains(wordList.get(i)) || wordList.get(i).equals(endWord)) {
        //             continue;
        //         }
        //         // 对比如果只差一个, 就放在队列里
        //         if (onlyOne(wordList.get(i), res.get(res.size() - 1))) {
        //             res.add(wordList.get(i));
        //             // 往下深度遍历?
        //             List<String> resTemp = find(res, wordList, endWord);
        //             if (resTemp == null) {
        //                 res.remove(res.size() - 1);
        //             } else {
        //                 return res;
        //             }
        //         } else {
        //             // 跳过
        //         }
        //     }
        //     // 都遍历完了没有拦截到,
        //     return null;
        // }
        public boolean onlyOne(String s1, String s2) {
            // 先看长度
            if (s1.length() != s2.length()) {
                return false;
            }

            int times = 0;
            for (int i = 0; i < s1.length(); i++) {
                if (s1.charAt(i) == s2.charAt(i)) {
                    continue;
                } else if (times == 0) {
                    times++;
                } else {
                    return false;
                }
            }
            return true;
        }


```


