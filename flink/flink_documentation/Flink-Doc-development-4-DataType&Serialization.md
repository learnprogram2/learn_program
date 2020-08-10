## DataTypes & Serialization

### Overview
Flink 处理dataType和序列化比较特殊, 包括flink里面的typeDescriptor, genericTypeExtraction和typeSerializationFramework. 本文描述了这个概念还有原理(rationale)

#### 1. 支持的数据类型
flink在stream/set里面的数据类型做了限制, 因为为了系统的分析数据类型来探测最有效地执行策略.
```java
// 可以有七种数据类型
Java Tuples and Scala Case Classes
Java POJOs
Primitive Types
Regular Classes
Values
Hadoop Writables
Special Types
```
1. Tuples and case classes:
... 不多说了.

2. POJOs:
需要满足一些规则的类就是POJO: public的class, 无参public构造器, getter/setter, field必须有支持的serializer.

3. 基本(Primitive)数据类型
Integer, String, Double 之类的.

4. General Class Types
遵循Java Beans规范的类基本都可以. 不满足POJO的就都当成generalClass处理了. generalType的序列化时使用的[Kryo](https://github.com/EsotericSoftware/kryo)

5. Values
value类型手动的制定serialization, 通过实现`org.apache.flinktypes.Value` 而不是指定一个序列化框架.  自己实现read,write. 标准的序列化效率低的时候就可以使用Value type了.
`CopyableValue`这个接口支持手动的指定内部克隆的逻辑.
flink准备了一些Value types对应基本数据类型(ByteValue, ShortValue, ...). 这些Value可以充当基本类型的替代品, 重用, 减少内存消耗.

6. Hadoop Writables.
可以实现`org.apache.hadoop.Writable`接口, 实现write的序列化逻辑. 

7. 特殊类型
也可以用特殊类型, 比如Scala的`Either,Option,Try`等, Java也有自己的Either的实现, 它表示一个value可能有两种类型, `left/right`. `Wither`可以在异常处理或者输出两种类型的时候使用.

8. 类型擦除(Type Erasure) & Type Inference
java编译器在编译之后会扔掉很多通用的类型信息(泛型). 这个在Java里叫泛型擦除. 在runtime, 泛型不知道是那个类型.
Flink在准备program来执行的时候需要类型信息, 















