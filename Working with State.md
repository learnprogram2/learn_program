# Working with State

## Keyed State 与 Operator State

Flink 中两种基本状态: KeyedState 和 OperatorState

### Keyed State

*Keyed State* 通常和 key 相关, 仅可使用在 `KeyedStream` 的方法和算子中. 分区或者共享的 Operator State, 而且每个 key 仅出现在一个分区. 和唯一元组 <算子并发实例, key> 绑定

### Operator State

每个 operator state 和一个并发实例进行绑定. 

