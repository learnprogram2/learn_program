[剑指 Offer 12. 矩阵中的路径](https://leetcode-cn.com/problems/ju-zhen-zhong-de-lu-jing-lcof/)

请设计一个函数，用来判断在一个矩阵中是否存在一条包含某字符串所有字符的路径。路径可以从矩阵中的任意一格开始，每一步可以在矩阵中向左、右、上、下移动一格。如果一条路径经过了矩阵的某一格，那么该路径不能再次进入该格子。例如，在下面的3×4的矩阵中包含一条字符串“bfce”的路径（路径中的字母用加粗标出）。

[["a","b","c","e"],
["s","f","c","s"],
["a","d","e","e"]]

但矩阵中不包含字符串“abfb”的路径，因为字符串的第一个字符b占据了矩阵中的第一行第二个格子之后，路径不能再次进入这个格子。

```text
输入：board = [["A","B","C","E"],["S","F","C","S"],["A","D","E","E"]], word = "ABCCED"
输出：true
```

思路: DFS: 要防止同一个位置不能经过两次.


```java
class Solution {
    public boolean exist(char[][] board, String word) {
        // dfs

        for (int i = 0; i < board.length; i ++) {
            for ( int j = 0; j < board[0].length; j ++) {
                if (exist(
                    board, i, j, word, 0
                ))
                    return true;
            }
        }
        return false;
    }

    public boolean exist(char[][] board, int i, int j, String word, int wi) {
        // 判断当前符不符合
        if (word.charAt(wi) != board[i][j]){
            return false;
        }
        // 干当前位子上的再去深度校验. 
        char temp = board[i][j];
        board[i][j] = '_';

        if (word.length()-1 == wi) {
            return true;
        }
        wi ++;
        
        // 开始以board[i,j]为原点, 四处遍历
        if (i+1 < board.length && exist(board, i+1, j, word, wi)){
            return true;
        } 
        if (j+1 < board[0].length && exist(board, i, j+1, word, wi)){
            return true;
        }
        if (i>0 && exist(board, i-1, j, word, wi)){
            return true;
        }
        if (j>0 && exist(board, i, j-1, word, wi)){
            return true;
        }
        // 校验不通过, 放回来
        board[i][j] = temp;
        return false;
    }
}
```



