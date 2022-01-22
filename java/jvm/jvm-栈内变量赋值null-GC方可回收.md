[Java 对象不使用时为什么要赋值为 null？](https://mp.weixin.qq.com/s/5C1sWKX7MtYg0yZp0VyWzA)

> 可达性分析算法, 栈内变量引用的对象, 就会被人存活的, 所以不会干掉.

### 栈内的变量 优化
```java
main:
    if (true) {
        int a = 1;
        int b = 2;
        int c = 3;
    }
    int d = 4;
```
理解为, 栈内的变量应该是[a, b, c, d );
事实上, 栈优化: [d, b, c ); a的定义域过期了, 所以被占用了.



