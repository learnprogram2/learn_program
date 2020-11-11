[剑指 Offer 43. 1～n 整数中 1 出现的次数](https://leetcode-cn.com/problems/1nzheng-shu-zhong-1chu-xian-de-ci-shu-lcof/)

### 解题思路

![image.png](https://pic.leetcode-cn.com/1605093034-IhKLxG-image.png)
![image.png](https://pic.leetcode-cn.com/1605093511-pMfTWz-image.png)


### 代码

```java
class Solution {

    public int countDigitOne(int n) {

        // [abcdefg], 对于d左侧导致的, 其实就是abc*10^("efg".length()).
        // 对于d右侧导致的: 分为d=0,1,2+. 0: 0, 1: efg+1, 2+: 10^("efg".length())
        String nStr = n + "";
        int sum = 0;
        for (int i = 0; i < nStr.length(); i++) {
            int curr = nStr.charAt(i) - '0';
            if (curr == 0) {
                // 左侧的数字导致的, 右侧变化此位不会是1
                int leftSum = nStr.substring(0, i).isEmpty() ? 0 : Integer.parseInt(nStr.substring(0, i)) * (int) Math.pow(10, nStr.length() - 1 - i);
                sum += leftSum;
            } else if (curr == 1) {
                // 左侧数字导致, 右侧数字导致的数值上限是数值
                int leftSum = nStr.substring(0, i).isEmpty() ? 0 : Integer.parseInt(nStr.substring(0, i)) * (int) Math.pow(10, nStr.length() - 1 - i);
                int rightSum = nStr.substring(i + 1).isEmpty() ? 1 : Integer.parseInt(nStr.substring(i + 1)) + 1;
                sum += leftSum;
                sum += rightSum;
            } else {
                // 左侧数字导致, 右侧也是定值
                int leftSum = nStr.substring(0, i).isEmpty() ? 0 : Integer.parseInt(nStr.substring(0, i)) * (int) Math.pow(10, nStr.length() - 1 - i);
                int rightSum = (int) Math.pow(10, nStr.length() - 1 - i);
                sum += leftSum;
                sum += rightSum;
            }
        }
        return  sum;

    }



//    int sum = 0;

//     public int countDigitOne(int n) {
//         // n这个数, 每个位上的i出现的1都可以分成两部分
//         // 1. 因为自己i位置上是1, 导致的出现10^(i-1)次1
//         // 2. 因为前面位子变化, 自己i位上出现的1
//         int temp = n;
//         int highI = 0;
//         while (temp / 10 != 0) {
//             temp = temp / 10;
//             highI++;
//         }
//         count(n, highI, 0);
//         return sum;
//     }

//     public int count(int n, int i, int leftSum) {
//         if (i < 0) {
//             return sum;
//         }
//         int tempI = i;
//         int tempN = n;
//         while (tempI > 0) {
//             tempN /= 10;
//             tempI--;
//         }
//         // 1.自己i位置导致的1
//         if (i > 0) {
//             sum += Math.pow(10, i) * (n / (int) Math.pow(10, i));
//         } else {
//             sum += n % 10;
//         }
//         // 2. left导致的1;
//         if (tempN % 10 > 0) {
//             sum += leftSum;
//         } else {
//             sum += leftSum - 1;
//         }
//         // 维护leftSum
//         leftSum *= 10;
//         leftSum += tempN % 10;

//         // 往下传递I
//         count(n, --i, leftSum);
//         return sum;
//     }
}
```