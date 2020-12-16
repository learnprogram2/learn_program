

## 12/14

### 01 | 时势与英雄：HTTP的前世今生

1. Http2相比于Http1.1: 
 二进制协议，不再是纯文本;
 可发起多个请求，废弃了 1.1 里的管道;
 使用专用算法压缩头部, 减少数据传输量;
 允许服务器主动向客户端推送数据;
 增强了安全性,"事实上"要求加密通信.



### 02 | HTTP是什么？HTTP又不是什么？

HTTP 是超文本传输协议(HyperText Transfer Protocol)
1. 协议: 计算机语言的通讯协议, 包括各种控制和Error处理方式.
2. 传输: 双向协议, 允许中转
3. 超文本: 文字/图片/音视频混合体.

HTTP不是:
1. 互联网
2. 编程语言
3. 不是HTML
4. 不是孤立的协议: 跑在TCP/IP协议栈上, 利用IP寻址路由, TCP实现可靠数据传输, DNS解析域名. 

![Http技术栈](https://static001.geekbang.org/resource/image/27/cc/2781919e73f5d258ff1dc371af632acc.png)



### 04 | HTTP世界全览（下）：与HTTP相关的各种协议
HTTPS全称是"HTTP over SSL/TLS", 也就是运行在 SSL/TLS 协议上的 HTTP. SSL/TLS是一个负责加密通信的安全协议, 建立在 TCP/IP 之上.
HTTPS 相当于"HTTP+SSL/TLS+TCP/IP", SSL 的全称是"Secure Socket Layer", 后来更名TLS, 所以叫SSL/TLS.






## 12/15

### 05 | 常说的“四层”和“七层”到底是什么？“五层”“六层”哪去了？

#### TCP/IP 网络分层模型
HTTP的下层协议TCP/IP 协议是一个"有层次的协议栈", 网络分层:
1. 应用层: application layer
2. 传输层: transport layer, 在IP标记的两点可靠传输: TCP, UDP不可靠.
3. 网际层: internet layer, IP协议取代Mac地址, 连接网络
4. 链接层: link layer, mac地址标记网卡层.
MAC 层的传输单位是帧（frame），IP 层的传输单位是包（packet），TCP 层的传输单位是段（segment），HTTP 的传输单位则是消息或报文（message）。但这些名词并没有什么本质的区分，可以统称为数据包。

#### OSI 网络分层模型
开放式系统互联通信参考模型
1. 第一层：物理层，网络的物理形式，例如电缆、光纤、网卡、集线器等等；
2. 第二层：数据链路层，它基本相当于 TCP/IP 的链接层；
3. 第三层：网络层，相当于 TCP/IP 里的网际层；
4. 第四层：传输层，相当于 TCP/IP 里的传输层；
5. 第五层：会话层，维护网络中的连接状态，即保持会话和同步；
6. 第六层：表示层，把数据转换为合适、可理解的语法和语义；
7. 第七层：应用层，面向具体的应用传输数据。
所谓的"四层负载均衡"就是指工作在传输层上, 基于TCP/IP协议的特性, 例如IP地址, 端口号等实现对后端服务器的负载均衡.

![两个分层之间的映射关系](https://static001.geekbang.org/resource/image/9d/94/9d9b3c9274465c94e223676b6d434194.png)






### 06 | 域名里有哪些门道？

IP地址是对物理网卡的Mac地址的抽象, 域名是对IP的抽象, 更容易记. DNS域名系统负责解析.
"time.geekbang.org", "org"就是顶级域名, "geekbang"是二级域名, "time"则是主机名. [服务于万维网(WWW)文件的机器会自动获得主机名"www"](https://zhuanlan.zhihu.com/p/107300977)

#### 域名的解析
![DNS树状结构](https://static001.geekbang.org/resource/image/6b/f2/6b020454987543efdd1cf6ddec784bf2.png)
1. 根域名服务器负责解析顶级域名服务器的IP
2. 顶级域名服务器负责解析二级域名IP
3. 权威域名服务器负责解析主机IP地址, 比如apple.com解析出www.apple.com的地址.

**域名解析過程: 浏览器缓存->操作系统缓存->hosts->dns**
1. 检查本地dns缓存是否存在解析"www.不存在.com"域名的ip
2. 如果没有找到继续查找本地hosts文件内是否有对应的固定记录
3. 如果hosts中还是没有那就根据本地网卡被分配的 dns server ip 来进行解析，dns server ip 一般是“非官方”的ip，比如谷歌的“8.8.8.8”，本身它也会对查找的域名解析结果进行缓存，如果它没有缓存或者缓存失效，则先去顶级域名服务器“com”去查找“不存在.com”的域名服务器ip，结果发现不存在，于是直接返回告诉浏览器域名解析错误，当然这两次查找过程是基于udp协议




## 12/16

对HTTP准确的称呼是"HTTP over TCP/IP",而另一个"HTTP over SSL/TLS"就是增加了安全功能的HTTPS.

### 08 | 键入网址再按下回车，后面究竟发生了什么？

#### a. 使用IP访问
![请求过程](https://static001.geekbang.org/resource/image/86/b0/86e3c635e9a9ab0abd523c01fc181cb0.png)
![请求过程图解](https://static001.geekbang.org/resource/image/8a/19/8a5bddd3d8046daf7032c7d60a3d1a19.png)
因为http/1连接传输效率低, 浏览器一般会对同一个域名发起多个连接提高效率, 4-6个包的端口52086就是开的第二个连接, 但在抓包中只是打开了, 还没有传输

#### b. 使用域名访问
多一步域名解析的过程, 之后使用IP访问. 域名指向的IP可能是一台DNS服务器, 然后对真正的服务器左DNS负载均衡.

#### c. 真实的网络请求
![请求步骤](https://static001.geekbang.org/resource/image/df/6d/df4696154fc8837e33117d8d6ab1776d.png)
1. DNS解析
	浏览器判断是不是ip地址，不是就进行域名解析，依次通过浏览器缓存，系统缓存，host文件，DNS服务器获取IP解析(解析失败的浏览器尝试换别的DNS服务器, 最终失败的进入错误页面)
2. 请求IP
	如果是CDN服务器, 先看是否缓存了, 缓存了响应用户，无法缓存，缓存失效或者无缓存，回源到服务器.
	经过防火墙外网网管路由到nginx接入层, ng缓存中存在的直接放回, 不存在的负载到web服务器. 
	web服务器接受到请后处理, 路径不存在404. 存在的返回结果(服务器中也会有redis,ehcache(堆内外缓存)，disk等缓存策略).
	原路返回，CDN加入缓存响应用户.




### 09 | HTTP报文是什么样子的？
HTTP基本不管传输, 由TCP进行发送和ACK之类的, HTTP核心部分就是传输的报文内容.

#### TCP报文结构
![TCP报文结构](https://static001.geekbang.org/resource/image/17/95/174bb72bad50127ac84427a72327f095.png)
到了目的地把头部去掉, 拿到数据.

**HTTP的报文内容:**
![请求报文](https://static001.geekbang.org/resource/image/1f/ea/1fe4c1121c50abcf571cebd677a8bdea.png)
![响应报文](https://static001.geekbang.org/resource/image/cb/75/cb0d1d2c56400fe9c9988ee32842b175.png)
1. 起始行(start line):描述请求或响应的基本信息; 
	- 请求行: 请求方法, 目标URI 版本号. POST /log HTTP/1.1
	- 响应的状态行: 版本号, 状态码, reason. HTTP/1.1 200 OK
2. header： key-value形式更详细地说明报文;
3. 正文(entity): 实际传输的数据, 文本, 图片, 视频等二进制数据.



### 10 | 应该如何理解请求方法？













