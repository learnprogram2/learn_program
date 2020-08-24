jvm dev现在的问题: 
1. 刚开始启动项目会出现3-5次meta space的FullGC
2. 13分钟会开始一个MinorGC, 频率还好. 但是每次minor gc大概要90-130ms. 时间比较长
    > 先jstat看新生代对象增长速率, 每次minor gc之后存活对象大小进去哪个区了.
    > 可能是eden区太大.








3. jobManager 优化: 增加了新生代的配比:
![](blob:https://yiming-wang.atlassian.net/ffd3ecf4-797b-48f7-b3ab-1674ae58c7e2#media-blob-url=true&id=943fa02b-f3ef-4b3e-b5dc-d9594d74987a&collection=contentId-37093553&contextId=37093553&mimeType=image%2Fpng&name=image-20200824-115758.png&size=320932&width=1542&height=760)

