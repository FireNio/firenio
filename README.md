
# BaseIO Project

[![License](https://img.shields.io/badge/License-Apache%202.0-F94F4F.svg)](https://github.com/generallycloud/baseio/blob/master/LICENSE.txt)
[![License](https://img.shields.io/badge/contact-email-green.svg)](mailto:wangkaicloud@163.com)

BaseIO是基于Java NIO开发的一款可快速构建网络通讯项目的异步IO框架，其以简单易用的API和优良的性能深受开发者喜爱。

## 项目特色

* 轻松实现断线重连(轻松实现心跳机制)
* 超过32W QPS(Socket)的处理速度(CPU_CORE_SIZE*8W)
* 简易应用容器：
 * 支持简易热部署，示例： https://www.generallycloud.com/system-redeploy
 * 支持部署WEB，微服务等（依据协议而定）
* 轻松实现简易负载均衡(可定制)，已知策略:
 * 基于hash的虚拟节点策略
 * 轮询负载节点策略
* 支持组件扩展，已知的扩展插件有：
 * 简易MQ，offer msg，poll msg
 * 简易实时UDP通讯，用作音/视频实时交互
 * 简易权限认证系统，用于限制单位时间内API调用次数
* 支持协议扩展，已知的扩展协议有：
 * Redis协议，示例：详见 {baseio-test}
 * Protobuf协议，示例：详见 {baseio-test}
 * LineBased协议（基于换行符的消息分割），示例：详见 {baseio-test}
 * FixedLength协议（固定长度报文头），支持传输文本和二进制数据
 * HTTP1.1协议（客户端，服务端），示例： https://www.generallycloud.com/
 * WebSocket协议（客户端，服务端），示例： https://www.generallycloud.com/web-socket/chat/index.html 
 * 私有协议（自己定义的协议报文头/协议报文体），支持传输文本和二进制数据及混合数据
 
## 如何使用

### 服务端：

```Java

	public static void main(String[] args) throws Exception {

		IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

			@Override
			public void accept(SocketSession session, ReadFuture future) throws Exception {
				future.write("yes server already accept your message:");
				future.write(future.getReadText());
				session.flush(future);
			}
		};
		
		SocketChannelContext context = new SocketChannelContextImpl(new ServerConfiguration(18300));
		
		SocketChannelAcceptor acceptor = new SocketChannelAcceptor(context);
		
		context.addSessionEventListener(new LoggerSocketSEListener());
		
		context.setIoEventHandleAdaptor(eventHandleAdaptor);
		
		context.setProtocolFactory(new FixedLengthProtocolFactory());

		acceptor.bind();
	}

```

### 客户端：

```Java

	public static void main(String[] args) throws Exception {

		IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

			@Override
			public void accept(SocketSession session, ReadFuture future) throws Exception {
				System.out.println();
				System.out.println("____________________"+future.getReadText());
				System.out.println();
			}
		};
		
		SocketChannelContext context = new SocketChannelContextImpl(new ServerConfiguration("localhost", 18300));

		SocketChannelConnector connector = new SocketChannelConnector(context);

		context.setIoEventHandleAdaptor(eventHandleAdaptor);
		
		context.addSessionEventListener(new LoggerSocketSEListener());

		context.setProtocolFactory(new FixedLengthProtocolFactory());
		
		SocketSession session = connector.connect();

		FixedLengthReadFuture future = new FixedLengthReadFutureImpl(context);

		future.write("hello server!");

		session.flush(future);
		
		ThreadUtil.sleep(100);

		CloseUtil.close(connector);
	}

```

###	详见 {baseio-test}

## 演示及用例
* HTTP Demo：https://www.generallycloud.com/index.html
* WebSocket聊天室 Demo：https://www.generallycloud.com/web-socket/chat/index.html                                
 （我写的后端，前端https://github.com/socketio/socket.io/ ）
* WebSocket小蝌蚪 Demo：https://www.generallycloud.com/web-socket/rumpetroll/index.html                                
 （我写的后端，前端https://github.com/danielmahal/Rumpetroll ）

## License

BaseIO is released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
