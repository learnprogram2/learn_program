

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


















