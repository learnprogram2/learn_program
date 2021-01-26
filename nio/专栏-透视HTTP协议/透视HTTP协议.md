

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

#### a. HTTP1.1 有8种请求方式
1. GET：获取资源，可以理解为读取或者下载数据；
2. HEAD：获取资源的元信息, 同GET, 但不会返回body数据
3. POST：向资源提交数据，相当于写入或上传数据；
4. PUT：类似 POST；
5. DELETE：删除资源；
6. CONNECT：建立特殊的连接隧道；
7. OPTIONS：列出可对资源实行的方法；
8. TRACE：追踪请求 - 响应的传输路径。


#### b. 安全与幂等

关于请求方法还有两个面试时有可能会问到、比较重要的概念：安全与幂等。
**安全** 请求方法不会破坏服务器的资源: GET和HEAD是安全的.
**幂等** 多次请求相同, GET/HEAD/DELETE/PUT是幂等的. post不是.








### 11 | 你能写出正确的网址吗？

URI 不完全等同于网址，它包含有 URL 和 URN 两个部分，在 HTTP 世界里用的网址实际上是 URL——统一资源定位符(Uniform Resource Locator)

#### URI 的格式
![URI格式](https://static001.geekbang.org/resource/image/ff/38/ff41d020c7a27d1e8191057f0e658b38.png)

1. schema: 资源的访问协议, HTTP/HTTPS/ftp/...
2. authority: 资源主机+端口+path
3. 

客户端看到的是完整的 URI, 使用特定的协议去连接特定的主机, 而服务器看到的只是报文请求行里被删除了协议名和主机名的 URI. 

#### URI 的编码

非 ASCII 码或特殊字符转换成十六进制字节值，然后前面再加上一个“%”。


1. HTTP 协议允许在在请求行里使用完整的 URI，但为什么浏览器没有这么做呢: header里面有;
2. URI 的查询参数和头字段很相似，都是 key-value 形式，都可以任意自定义，那么它们在使用时该如何区别呢？（具体分析可以在“答疑篇”第 41 讲中的 URI 查询参数和头字段部分查看）
query参数针对的是资源（uri），而字段针对的是本次请求，也就是报文。



## 12/21

### 12 | 响应状态码该怎么用？

响应的状态行: "Version status_code reason": "HTTP/1.1 200 OK"

#### 状态码: 
000->999. 三位数, 分为五类:
1××：提示信息，表示目前是协议处理的中间状态，还需要后续的操作； 
	101 Switching Protocols: webSocket更改HTTP协议的时候就会发送
2××：成功，报文已经收到并被正确处理；
	200 OK
	204 ok no content
	206 ok partial content
3××：重定向，资源位置发生变动，需要客户端重新发送请求；
	301 moved permanently 永久重定向
	302 moved temporarily 暂时重定向, 在header的location指定后续跳转URL.
	304 not modified 可以用缓存.
4××：客户端错误，请求报文有误，服务器无法处理；
	400 badrequest: 请求参数有问题
	403 forbidden
	404 not found
	...
5××：服务器错误，服务器在处理请求时内部发生了错误。
	500 internal server error
	501 not implemented
	502 bad gateway
	503 service unavailable


### 13 | HTTP有哪些特点？

![特点](https://static001.geekbang.org/resource/image/78/4a/7808b195c921e0685958c20509855d4a.png)


对比 UDP协议, 不过它是无连接也无状态的, 顺序发包乱序收包，数据包发出去后就不管了，收到后也不会顺序整理。而 HTTP 是有连接无状态，顺序发包顺序收包，按照收发的顺序管理报文。

### 14 | HTTP有哪些优点？又有哪些缺点？

- 明文传输, HTTPS
- 性能不够好, HTTP/2 和 HTTP/3




## 12/30 HTTP协议内容

### 15 | 海纳百川：HTTP的实体数据
#### a. 数据类型与编码
1. Accept: 是MIME type 标记body内容.
2. Accept-Encoding: 标记body数据格式.
#### b. 语言类型与编码
1. 请求头: Accept-Language, Accept-Charset: gbk, utf-8
2. 响应头: Content-Language, Content-Type: text/html; charset=utf-8

#### c. 内容协商
Accept 等头字段可以用“,”顺序列出多个可能的选项，还可以用“;q=”参数来精确指定权重:
`Accept: text/html,application/xml;q=0.9,*/*;q=0.8`




### 16 | 把大象装进冰箱：HTTP传输大文件的方法


1. 数据压缩: 通过content-encoding告诉浏览器.
2. 分块传输: 响应头`Transfer-Encoding: chunked`, 把文件分块.
3. 范围请求: 请求一个文件的一部分




### 17 | 排队也要讲效率：HTTP的连接管理

#### 短连接
HTTP(0.9/1.0), 在传输完之后马上断掉TCP/IP, 是短连接.
TCP链接三次握手, 关闭四次挥手.

#### 长连接
HTTP1.1默认长连接: `Connection: keep-alive`.


#### 队头阻塞
如果长连接里第一个阻塞了, 那么之后也会阻塞.
1. 并发连接: 对一个域名发起多个长连接.
2. 域名分片: 多开几个域名, 指向同一台服务器. 解决客户端的限制.




### 18 | 四通八达：HTTP的重定向

#### 外部重定向的过程
1. 响应301/302, 加一个location:/uri/to/resource
2. 永久/临时重定向: 301/302

#### 内部重定向
是一个router的概念.


### 19 | 让我知道你是谁：HTTP的Cookie机制



### 20 | 生鲜速递：HTTP的缓存控制

#### a. 服务器的缓存控制
浏览器向服务器请求资源, 服务器响应资源, 并标记有效期. 使用Header的"Cache-Control"标记

#### b. 条件请求
验证资源是否失效, 常用的是“if-Modified-Since”和“If-None-Match”，收到 304 就可以复用缓存里的资源;




## 1/06

### 21 | 良心中间商：HTTP的代理服务

![Http代理](https://static001.geekbang.org/resource/image/28/f9/28237ef93ce0ddca076d2dc19c16fdf9.png)
代理服务本身不生产内容，而是处于中间位置转发上下游的请求和响应，具有双重身份.

#### a. 代理的作用

负载均衡, 解决其他的几乎所有的增强功能. 正向代理: 代理client, 反向代理: 代理server.

#### b. 代理相关头字段
如果双方要获取原始信息: 
1. 代理服务器需要用字段“Via”标明自己的代理身份 多个代理会追加via字段, 形成链表: "Via: proxy1, proxy2"
2. "X-Forwarded-For"表示主机IP, "X-Real-IP"标识客户端的IP






## 01/26

### 22 | 冷链周转：HTTP的缓存代理

Http缓存控制+代理服务合起来就是缓存代理.

1. HTTP 的服务器缓存功能主要由代理服务器来实现（即缓存代理）
2. Http-client端缓存

#### a. 缓存代理服务
![代理缓存](https://static001.geekbang.org/resource/image/5e/c2/5e8d10b5758685850aeed2a473a6cdc2.png)
1. 缓存代理服务器会把后台服务器的响应缓存到cache里
2. 下次相同请求, 缓存代理服务器直接发送304(未变化), 或者cache里的缓存数据

#### b. 缓存控制
1. 缓存代理的缓存控制
![缓存代理服务器的缓存控制流程](https://static001.geekbang.org/resource/image/09/35/09266657fa61d0d1a720ae3360fe9535.png)

2. 客户端的缓存控制
![客户端的缓存控制](https://static001.geekbang.org/resource/image/47/92/47c1a69c800439e478c7a4ed40b8b992.png)






# 安全篇

### 23 | HTTPS是什么？SSL/TLS又是什么？
Http的无状态, 可以通过cookie解决, 明文需要用Https协议. HTTPS 的安全性是由 TLS保证

#### a. 通讯安全
1. 机密性(secery/Confidentiality): 数据的保密, 只能可信的人访问
2. 完整性(Integrity): 数据的传输不能被篡改.
3. 身份认证(authentication): 消息指发送给可信的人.
4. 不可否认(non-repudiation/undeniable): 不能抵赖.

#### b. HttpS是什么
除了协议名"http"和端口号80不同.HTTPS协议在语法, 语义上和 HTTP 完全一样. 
抓包, 多了"Client Hello" 和 "Server Hello"等新的数据包. "HTTP over TCP/IP"变成了"HTTP over SSL/TLS"
![HTTPS协议](https://static001.geekbang.org/resource/image/50/a3/50d57e18813e18270747806d5d73f0a3.png)

#### c. SSL/TLS
- SSL 即安全套接层(Secure Sockets Layer), OSI模型的第五层(会话层). SSL在3.0时候改名TLS(Transport layer security)1.0
- SSL/TLS 是信息安全领域中的权威标准，采用多种先进的加密技术保证通信安全
- 机密性由对称加密AES保证，完整性由SHA384摘要算法保证，身份认证和不可否认由RSA非对称加密保证
```text
实验环境使用的 TLS 是 1.2，客户端和服务器都支持非常多的密码套件，而最后协商选定的是“ECDHE-RSA-AES256-GCM-SHA384”
```




### 24 | 固若金汤的根本（上）：对称加密与非对称加密

![对称加密](https://static001.geekbang.org/resource/image/8f/49/8feab67c25a534f8c72077680927ab49.png)
1. 对称加密: 加密和解密使用的密钥是同一个. TLS可以用多种对称加密算法, 常用AES 和 ChaCha20.
    - AES:Advanced Encryption Standard, 高级加密标准, 密钥长度可选128/192/256. 性能挺好的.
2. 对称加密的加密分组模式: 让算法用固定长度的密钥加密解密任意长度明文.
    - AEAD: 在加密的时候增加了认证功能
    - GCM: 例如AES128-GCM: 密钥长度128位的AES对称加密算法, 分组模式是GCM.


![非对称加密](https://static001.geekbang.org/resource/image/89/17/89344c2e493600b486d5349a84318417.png)
1. 非对称加密: (私钥解密, 公钥加密. 私钥签名, 公钥解签名). 运算复杂, 加密需要更多位数
    - RSA: 最常用(2048位比较安全)
    - ECC: 新秀.
2. 混合加密: 性能和安全兼顾.
    1. 通讯刚开始使用非对称加密: 解决密钥交换问题
    2. 随机数产生*会话密钥*(session key). 使用公钥加密.
    3. server端使用私钥解密, 取出session-key, 然后就用它做 对称加密.
    ![混合加密](https://static001.geekbang.org/resource/image/e4/85/e41f87110aeea3e548d58cc35a478e85.png)

3. 简单理解TLS: 通信双方通过非对称加密协商出一个用于对称加密的密钥.


### 25 | 固若金汤的根本（下）：数字签名与证书
混合加密还没有解决的漏洞: 完整性, 身份认证.
1. (请求不完整也会处理)黑客收集足够多的通讯, 然后一起发给server, 造成server阻塞.
2. (通讯不做身份认证)黑客拦截server响应, 发送乱序的. 或者发布假公钥, 冒充server. 

#### a. [完整性]摘要算法(Digest Algorithm)
![完整性](https://static001.geekbang.org/resource/image/c2/96/c2e10e9afa1393281b5633b1648f2696.png)
1. hash映射. 单向压缩算法, 无法解密, 把数据压缩成独一无二的固定长度字符, 作为数据的"指纹". MD5, SHA-1最常见, 推荐SHA-2.
2. 完整性: 在数据后面附上SHA算法摘要来保证. 还要建立在所有数据的密文传输上.

#### b. [身份认证]数字签名
![数字签名原理](https://static001.geekbang.org/resource/image/84/d2/84a79826588ca35bf6ddcade027597d2.png)
1. client公钥加密请求server, server对client的请求的摘要做私钥加密, 响应
2. client收到后, 用公钥解密看看原来的摘要是不是被私钥加密了一遍, 如果是就是真正的server签名(加密)了.

#### c. [避免假公钥]数字证书和 CA
CA(Certificate Authority) 证书认证机构.

**数字证书**: 公钥, 序列号, 用途, 颁发者, 有效时间.
![CA证书体系](https://static001.geekbang.org/resource/image/8f/9c/8f0813e9555ba1a40bd2170734aced9c.png)
小CA靠大CA认证自己, 和域名解类似的树状, 最终由RootCA"自签名证书/根证书".
自签名证书不被浏览器信任, 单把它放到系统根证书存储区里, 就会被信任.
- 整个体系在于信任: CA可能被攻击, 证书可能会过期/错误.
- 流程:
1. 服务器返回的是证书链(不包括根证书,根证书预置在浏览器中).
2. 浏览器就可以使用自己存的的根证书解析证书链的根证书得到一级证书的公钥+摘要验签, 
3. 然后拿一级证书的公钥解密一级证书拿到二级证书的公钥和摘要验签.
4. 再然后拿二级证书的公钥解密二级证书得到服务器的公钥和摘要验签, 验证过程就结束了.



### 26 | 信任始于握手：TLS1.2连接过程解析

#### a. HTTPS 建立连接

1. DNS域名解析到服务器IP. 
2. TCP三次握手建立连接(注意Https默认端口443)

3. TLS建立安全连接.

#### b. TLS协议包含几个子协议

子协议负责不同职责:

1. **record protocol: 记录协议:** 规定了TLS收发数据的基本单位是record, 像TCP里的segment. 所有的子协议数据都要通过record-protocol发处, 多个record可以在TCP包里一次性发出.
2. **Alert protocol: 报警协议**: 向对方发出报警信息.像HTTP里的状态码: protocol_version不支持旧版本, bad_certificate:证书有问题.
3. **Handshake protocol: 握手协议:** 最复杂的子协议, 浏览器和服务器在握手中协商 TLS 版本号, 随机数, 密码套件等信息.

4. **Change Cipher Spec Protocol: 变更密码规范协议:** 告诉对方, 后续的数据都将使用加密保护, 之前是明文.

![img](https://static001.geekbang.org/resource/image/69/6c/69493b53f1b1d540acf886ebf021a26c.png)

#### c. ECDHE 握手过程

![image-20210126205700063](%E9%80%8F%E8%A7%86HTTP%E5%8D%8F%E8%AE%AE.assets/image-20210126205700063.png)

#### 总结

1. HTTPS 协议会先 三次TCP 握手, 然后执行 TLS 握手, 才能建立安全连接;
2. 握手的目标是安全地交换对称密钥, 需要三个随机数, 第三个随机数"Pre-Master"必须加密传输,绝对不能让黑客破解;
3. "Hello"消息交换随机数, "Key Exchange"消息交换"Pre-Master";
4. "Change Cipher Spec"之前传输的都是明文, 之后都是对称密钥加密的密文.

**我的理解:** 

HTTPS协议是在TCP协议上加了TLS安全协议, 通过TLS握手来交给client公钥(并通过CA链来验证公钥真伪), 通过非对称加密协商对称密钥,  然后对之后的HTTP协议来往做对称加密. 实现安全和效率的平衡.



### 27 | 更好更快的握手：TLS1.3特性解析

TLS1.3 的三个主要改进目标：兼容、安全与性能。

1. 为了兼容 1.1、1.2 等“老”协议，TLS1.3 会“伪装”成 TLS1.2，新特性在“扩展”里实现；
2. TLS1.3 大幅度删减了不安全的加密算法.
3. TLS1.3 也简化了握手过程，完全握手由原来2个消息往返到一个消息往返.

**RSA 密钥交换不具有“前向安全”:** 

虽然每次 TLS 握手中的会话密钥都是不一样的, 但服务器的私钥却始终不会变. 一旦黑客拿到了服务器私钥, 并且截获了之前的所有密文, 就能拿到每次会话中的对称密钥.



### 28 | 连接太慢该怎么办：HTTPS的优化

HTTPS分为两个部分:

1. 建立连接时的非对称加密握手;
2. 第二个是握手后的对称加密报文传输.

**优化思路:**

1. 硬件优化加密和解密的计算, 软件手段减少网络耗时和计算耗时. 



### 29 | 我应该迁移到HTTPS吗？

经过适当优化之后，HTTPS 的额外 CPU 成本小于 1%，额外的网络成本小于 2%.

1. Https不会慢很多.
2. 贵: Https由免费的CA证书.
3. 难: Https涉及到的知识很多, 但配置就可以了.
4. 配置 HTTPS 时需要注意选择恰当的 TLS 版本和密码套件, 强化安全;

**重定向跳转:** 默认使用 HTTP 协议访问, 使用301永久跳转到新的https地址.





# 飞翔篇

### 30 | 时代之风（上）：HTTP/2特性概览

HTTPS，通过引入 SSL/TLS 在安全上达到了“极致”, 性能不行. HTTPS 逐渐成熟之后, HTTP 就向着性能方面开始"发力".

**为什么不是 HTTP/2.0:** HTTP在2以后不用小版本了.

#### a.**兼容 HTTP/1**

HTTPS已经很安全了, 所以HTTP/2唯一目标就是改善性能.

1. **语义层完全一致:** 

   请求方法, URI, 状态码, 头字段.... 不变.

   HTTP/2 没有在 URI 里引入新的协议名, http表示明文的http/2, https表示加密的.

2. **语法层: 翻天覆地, 改变了HTTP报文的传输格式.**

#### b. 头部压缩

HTTP/1只有body压缩, HTTP/2开发了专门的"HPACK"算法, 在客户端和服务器两端建立"字典", 用索引号表示重复的字符串, 还釆用哈夫曼编码来压缩整数和字符串, 可以达到 50%~90% 的高压缩率.

#### c. 二进制格式

HTTP/2把TCP 协议的部分特性挪到了应用层, 把原来的"Header+Body"的消息"打散"为数个小片的二进制"帧"(Frame), 用"HEADERS"帧存放头数据, "DATA"帧存放实体数据.

![image-20210127000741392](%E9%80%8F%E8%A7%86HTTP%E5%8D%8F%E8%AE%AE.assets/image-20210127000741392.png)

#### d. 虚拟的“流”

消息的“碎片”到达目的地后应该怎么组装起来呢？

![image-20210127002123342](%E9%80%8F%E8%A7%86HTTP%E5%8D%8F%E8%AE%AE.assets/image-20210127002123342.png)

- http/2 服务器可以主动发送数据到客户端, 增快.



#### e. 协议栈

HTTP/2不强制加密, 但实际上是建立在TLS1.2及以上的. h2”表示加密的 HTTP/2，“h2c”表示明文的 HTTP/2. h2c不需要TLS握手以及加解密.

![image-20210127002351435](%E9%80%8F%E8%A7%86HTTP%E5%8D%8F%E8%AE%AE.assets/image-20210127002351435.png)



### 31 | 时代之风（下）：HTTP/2内核剖析

#### a. 连接前言

TLS 握手成功之后, 客户端必须要发送一个“连接前言”(connection preface), 用来确认建立 HTTP/2 连接.

```txt
// 标准的 HTTP/1 请求报文, 纯文本的 ASCII 码格式
PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n
```

#### b. 头部压缩

HTTP/2的报文还是由“Header+Body”构成的, 但在请求发送前, 必须要用“HPACK”算法来压缩头部数据. “HPACK”算法是专门为压缩 HTTP 头部定制的算法, “有状态”的算法，需要客户端和服务器各自维护一份“索引表”.

1. 废除了起始行, 把请求方法/URI/状态码等 转换成了头字段的形式. **伪头字段**, 用:作为名字开头.

#### c. 二进制帧

![image-20210127005213578](%E9%80%8F%E8%A7%86HTTP%E5%8D%8F%E8%AE%AE.assets/image-20210127005213578.png)



#### d: ! 流与多路复用

流是二进制帧的双向传输序列, 帧是乱序收发的, 但只要它们都拥有相同的流 ID, 就都属于一个流, 而且在这个流里帧不是无序的, 而是有着严格的先后顺序.



#### e. 流状态转换

HTTP/2 借鉴了 TCP，根据帧的标志位实现流状态转换。

<img src="%E9%80%8F%E8%A7%86HTTP%E5%8D%8F%E8%AE%AE.assets/image-20210127010147419.png" alt="image-20210127010147419" style="zoom: 33%;" />

#### 总结

1. HTTP/2 必须先发送一个“连接前言”字符串，然后才能建立正式连接；
2. HTTP/2 废除了起始行，统一使用头字段，在两端维护字段“Key-Value”的索引表，使用“HPACK”算法压缩头部；
3. HTTP/2 把报文切分为多种类型的二进制帧，报头里最重要的字段是流标识符，标记帧属于哪个流；
4. 流是 HTTP/2 虚拟的概念，是帧的双向传输序列，相当于 HTTP/1 里的一次“请求 - 应答”；
5. 在一个 HTTP/2 连接上可以并发多个流，也就是多个“请求 - 响应”报文，这就是“多路复用”。



### 32 | 未来之路：HTTP/3展望

#### a. HTTP/2 的“队头阻塞”













