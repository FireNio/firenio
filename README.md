
# NimbleIO使用指南

本项目目前还在测试阶段，请大家多提issue，共同完善。

## 项目意义

学习研究，目标成为一个易用且不失性能的IO工具包

## 项目特色

* 无差别 [服务端/客户端 ] 转换
* IO读写分离
* 轻松实现简易负载均衡
* 自带心跳包
* 超过10W OPS(Socket)的处理速度
* 弱网络环境下优良的性能表现
* 支持组件扩展，目前的扩展插件有：
 * 简易MQ，offer msg，poll msg
 * 简易实时UDP通讯，用作音/视频实时交互
 * 简易权限认证系统，用于限制单位时间内API调用次数
* 支持协议扩展，目前的扩展协议有：
 * HTTP1.1协议（小部分），支持请求动静态内容，上传下载附件
 * 私有协议（自己定义的协议报文头/协议报文体），支持传输文本和数据流

## 功能列表

### 详见 {src\test\java\test}，各种用法


## 案例
案例地址：https://github.com/NimbleIO/android-chat-starter

	(感谢android-chat-starter@https://github.com/madhur/android-chat-starter)
![](https://raw.githubusercontent.com/NimbleIO/NimbleIO/master/images/TEST-1.png)


