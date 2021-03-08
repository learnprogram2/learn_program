B+树的root节点类似SSTable的hashTable, 可以使用二分法定位数据所在的数据页.
1. InnoDB每个数据页大小16KB, 可以粗算Root节点存放的(子节点指针6byte, 节点最大ID8byte)14byte, 大概能存16kb/14byte=1170.
2. 那么看每个数据页存多少数据, 16kb, 如果每条数据1kb, 那么每个数据页存16条
3. 如果是二层B+树, 索引页->数据页: 1170 * 16 条
4. 如果是三层B+树, 索引页->索引页->数据页: 1170 * 1170 * 16条 = 2kw+


https://blog.csdn.net/qq_35590091/article/details/107361172
