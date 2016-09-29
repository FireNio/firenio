
# NimbleIO Project

NimbleIO是基于Java NIO开发的一款可快速构建网络通讯项目的异步IO框架，其以简单易用的API和优良的性能深受开发者喜爱。

## 如何使用

	详见 {nimbleio-test}

## 项目特色

* IO读写分离
* 支持内存池
* 支持内存0拷贝
* 轻松实现简易负载均衡
* 无差别 [服务端/客户端 ] 转换
* 超过12W QPS(Socket)的处理速度
* 超过 4W QPS(HTTP)  的处理速度
* 弱网络环境下优良的性能表现
* 轻松实现断线重连(轻松实现心跳机制)
* 支持组件扩展，目前的扩展插件有：
 * 简易MQ，offer msg，poll msg
 * 简易实时UDP通讯，用作音/视频实时交互
 * 简易权限认证系统，用于限制单位时间内API调用次数
* 支持协议扩展，目前的扩展协议有：
 * HTTP1.1协议（小部分），支持请求动静态内容，上传下载附件（待完善）
 * WebSocket协议（小部分），基于此开发了一套聊天室示例
 * 私有协议（自己定义的协议报文头/协议报文体），支持传输文本和数据流
 * 私有协议（4位字节表示报文长度），支持传输文本

## 演示及用例
* HTTP Demo：http://www.generallycloud.com/index.html
* WebSocket聊天室 Demo：http://www.generallycloud.com/web-socket/chat/index.html                                
 （我写的后端，前端https://github.com/socketio/socket.io/ ）
* WebSocket小蝌蚪 Demo：http://www.generallycloud.com/web-socket/rumpetroll/index.html                                
 （我写的后端，前端https://github.com/danielmahal/Rumpetroll ）
