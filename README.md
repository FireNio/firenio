
# NimbleIO使用指南

本项目目前还在测试阶段*3，有很多问题，请大家多提issue，共同完善。

## 项目意义

学习研究，目标成为一个易用且不失性能的IO工具包

## 项目特色

* IO读写分离
* 支持内存池
* 支持内存0拷贝
* 轻松实现心跳机制
* 轻松实现简易负载均衡
* 无差别 [服务端/客户端 ] 转换
* 超过12W QPS(Socket)的处理速度(服务端：I7 *70%,客户端：I5 *100%；百兆网卡IO *100%)
* 超过 4W QPS(HTTP)  的处理速度(服务端：I7 *30%,客户端：I5 *100%；百兆网卡IO *70%)
* 弱网络环境下优良的性能表现
* 支持组件扩展，目前的扩展插件有：
 * 简易MQ，offer msg，poll msg
 * 简易实时UDP通讯，用作音/视频实时交互
 * 简易权限认证系统，用于限制单位时间内API调用次数
* 支持协议扩展，目前的扩展协议有：
 * HTTP1.1协议（小部分），支持请求动静态内容，上传下载附件
 * WebSocket协议（小部分），可以聊天什么的
 * 私有协议（自己定义的协议报文头/协议报文体），支持传输文本和数据流
 * 私有协议（4位字节表示报文长度），支持传输文本

## 功能列表

详见 {src\test\java\test}，各种用法

## 演示及用例
* HTTP Demo：http://www.generallycloud.com/index.html
* WebSocketChat Demo：http://www.generallycloud.com/web-socket/chat/index.html                                
 （我写的后端，前端https://github.com/socketio/socket.io/ ）
* 小蝌蚪 Demo：http://www.generallycloud.com/web-socket/rumpetroll/index.html                                
 （我写的后端，前端https://github.com/danielmahal/Rumpetroll ）
