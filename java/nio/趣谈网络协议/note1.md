## 通信协议综述

### 1. 为什么要学网络协议

1. 协议三要素:

   - 语法: 符合格式
   - 语义: make sense. 有正确的含义.
   - 顺序: 指令内容要有顺序

   ![img](note1.assets/5c00f6e610f533d17fb4ad7decacc776.jpg)



### 2. 网络分层的真实含义

1. 网络为什么要分层?

   **复杂程序都需要分层.** 程序设计要求的

2. 程序是如何工作的?

只要是在网络上跑的包，都是完整的。可以有下层没上层，绝对不可能有上层没下层。





### 3. Ifconfig: 查看IP

IP:

子网掩码: 根据子网掩码计算IP的网络号

广播地址: 一般是IP的主机号部分都是1的.

CIDR: 用子网掩码和IP计算网络号.



### 4. DHCP和PXE: IP的由来



1. 发送请求, Linux会判断目标IP和自己的IP是不是一个网段的
   - 一个网段的: 发送ARP请求获取对方Mac地址, 把包发给交换机
   - 不是一个网段: 获取网关的Mac地址, 把包发送给网关

2. DHCP(dynamic host configuration protocol) 动态主机配置协议.

   在网段里配置一段共享的 IP 地址, 每一台新接入的机器都通过 DHCP 协议在共享的 IP 地址里申请, 然后自动配置好.
   
   1. 机器新加入一个网络, 大喊一声: DHCP Discover
   2. DHCP-Sever收到请求, 提供DHCP Offer
   3. 机器收到(可能多个DHCP-Server)n个DHCP-Offer, 选择最早的, 然后广播给所有人, 自己选择了哪个DHCP-Server的哪个IP.
   4. 对应的DHCP-Server收到request之后, 广播DHCP-ACK包. 然那个大家都知道

3. IP 地址的收回和续租

   在租约过去50%之后, 机器向DHCP-Server发送DHCP-request续约
   
   
   
   

### 

1. 一层设备: 集线器, 广播所有的数据
2. 二层设备: 交换机, 转发mac地址的数据





















