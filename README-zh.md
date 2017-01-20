
# BaseIO Project

[![License](https://img.shields.io/badge/License-Apache%202.0-585ac2.svg)](https://github.com/generallycloud/baseio/blob/master/LICENSE.txt)
[![Maven central](https://img.shields.io/badge/maven%20central-3.1.8.Alpha1-green.svg)](http://mvnrepository.com/artifact/com.generallycloud/baseio-all)
[![Website](https://img.shields.io/badge/website-generallycloud-green.svg)](https://www.generallycloud.com)

BaseIO是基于Java NIO开发的一款可快速构建网络通讯项目的异步IO框架，其以简单易用的API和优良的性能深受开发者喜爱。

## 项目特色

* 轻松实现断线重连(轻松实现心跳机制)
* 简易应用容器：
 * 支持简易热部署，示例： https://www.generallycloud.com/system-redeploy
 * 支持部署WEB，微服务等（依据协议而定）
* 轻松实现简易负载均衡(可定制)，已知策略:
 * 基于hash的虚拟节点策略
 * 轮询负载节点策略
* 支持组件扩展，已知的扩展插件有：
 * 简易MQ，offer msg，poll msg
 * 简易实时rtp(udp)，用作音/视频实时交互
 * 简易权限认证系统，用于限制单位时间内API调用次数
* 支持协议扩展，已知的扩展协议有：
 * Redis协议，示例：详见 {baseio-test}
 * Protobuf协议，示例：详见 {baseio-test}
 * LineBased协议（基于换行符的消息分割），示例：详见 {baseio-test}
 * FixedLength协议（固定长度报文头），支持传输文本和二进制数据
 * HTTP1.1协议（客户端，服务端），示例： https://www.generallycloud.com/
 * WebSocket协议（客户端，服务端），示例： https://www.generallycloud.com/web-socket/chat/index.html 
 * 私有协议（自己定义的协议报文头/协议报文体），支持传输文本和二进制数据及混合数据
* 压力测试
 * 超过320K QPS的处理速度(Socket,I7-4790,Win10)
 * 超过200K QPS的处理速度(Http1.1,I7-4790,Win10)  [ab压测](/baseio-documents/load-test/load-test-http.txt)
 
## 快速入门

 * Maven引用：

  ```xml  
	<dependency>
		<groupId>com.generallycloud</groupId>
		<artifactId>baseio-all</artifactId>
		<version>3.1.8-Alpha1</version>
	</dependency>  
  ```
  
* 服务端：

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

* 客户端：

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
 （后端基于baseio，前端基于：https://github.com/socketio/socket.io/ ）
* WebSocket小蝌蚪 Demo：https://www.generallycloud.com/web-socket/rumpetroll/index.html                                
 （后端基于baseio，前端基于：https://github.com/danielmahal/Rumpetroll ）

## License

BaseIO is released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## 了解更多，加入该项目QQ群，更多java相关技术均可在此讨论
 * QQ群号码：540637859
 * 点击链接加入：[![img](http://pub.idqqimg.com/wpa/images/group.png)](http://shang.qq.com/wpa/qunwpa?idkey=2bd71e10d876bb6035fa0ddc6720b5748fc8985cb666e17157d17bcfbd2bdaef)
 * 扫码加入：<br />  ![image](/baseio-documents/popularize/java-io-group-code-big.png)
