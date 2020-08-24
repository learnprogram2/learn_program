jvm dev现在的问题: 
1. 刚开始启动项目会出现3-5次meta space的FullGC
2. 13分钟会开始一个MinorGC, 频率还好. 但是每次minor gc大概要90-130ms. 时间比较长
    > 先jstat看新生代对象增长速率, 每次minor gc之后存活对象大小进去哪个区了.
    > 可能是eden区太大.
