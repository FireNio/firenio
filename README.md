
# NimbleIO Project

NimbleIO是基于Java NIO开发的一款可快速构建网络通讯项目的异步IO框架，其以简单易用的API和优良的性能深受开发者喜爱。

## 项目特色

* 轻松实现断线重连(轻松实现心跳机制)
* 超过12W QPS(Socket)的处理速度
* 轻松实现简易负载均衡，可定制负载均衡策略
 * 已知策略：基于虚拟节点的散列分布策略；轮询负载节点策略
* 支持组件扩展，已知的扩展插件有：
 * 简易MQ，offer msg，poll msg
 * 简易实时UDP通讯，用作音/视频实时交互
 * 简易权限认证系统，用于限制单位时间内API调用次数
* 支持协议扩展，已知的扩展协议有：
 * HTTP1.1协议（小部分），支持请求动静态内容，上传下载附件（待完善）
 * WebSocket协议（小部分），基于此开发了一套聊天室示例
 * 私有协议（自己定义的协议报文头/协议报文体），支持传输文本和数据流
 * 私有协议（4位字节表示报文长度），支持传输文本
 
## 如何使用

### 服务端：

```Java

public static void main(String[] args) throws Exception {

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				FixedLengthReadFuture f = (FixedLengthReadFuture) future;
				String res = "yes server already accept your message:" + f.getText();
				future.write(res);
				session.flush(future);
			}
		};

		ServerConfiguration configuration = new ServerConfiguration();
		
		configuration.setSERVER_TCP_PORT(18300);

		SocketChannelAcceptor acceptor = new SocketChannelAcceptor();

		EventLoopGroup eventLoopGroup = new SingleEventLoopGroup(
				"IOEvent",
				configuration.getSERVER_CHANNEL_QUEUE_SIZE(),
				configuration.getSERVER_CORE_SIZE());

		NIOContext context = new DefaultNIOContext(configuration, eventLoopGroup);
		
		context.addSessionEventListener(new LoggerSEListener());
		
		context.addSessionEventListener(new SessionAliveSEListener());

		context.setIOEventHandleAdaptor(eventHandleAdaptor);
		
		context.setBeatFutureFactory(new FLBeatFutureFactory());

		context.setProtocolFactory(new FixedLengthProtocolFactory());

		acceptor.setContext(context);

		acceptor.bind();
	}

```

### 客户端：

```Java

public static void main(String[] args) throws Exception {

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {

				FixedLengthReadFuture f = (FixedLengthReadFuture) future;
				System.out.println();
				System.out.println("____________________"+f.getText());
				System.out.println();
			}
		};

		SocketChannelConnector connector = new SocketChannelConnector();
		
		ServerConfiguration configuration = new ServerConfiguration();
		
		configuration.setSERVER_HOST("localhost");
		configuration.setSERVER_TCP_PORT(18300);
		
		EventLoopGroup eventLoopGroup = new SingleEventLoopGroup(
				"IOEvent", 
				configuration.getSERVER_CHANNEL_QUEUE_SIZE(),
				1);

		NIOContext context = new DefaultNIOContext(configuration,eventLoopGroup);

		context.setIOEventHandleAdaptor(eventHandleAdaptor);
		
		context.addSessionEventListener(new LoggerSEListener());

		context.addSessionEventListener(new ConnectorCloseSEListener(connector));

		context.addSessionEventListener(new SessionActiveSEListener());
		
		context.setBeatFutureFactory(new FLBeatFutureFactory());

		context.setProtocolFactory(new FixedLengthProtocolFactory());
		
		connector.setContext(context);
		
		connector.connect();

		Session session = connector.getSession();

		ReadFuture future = new FixedLengthReadFutureImpl();

		future.write("hello server !");

		session.flush(future);
		
		ThreadUtil.sleep(100);

		CloseUtil.close(connector);
	}

```

###	详见 {nimbleio-test}

## 演示及用例
* HTTP Demo：http://www.generallycloud.com/index.html
* WebSocket聊天室 Demo：http://www.generallycloud.com/web-socket/chat/index.html                                
 （我写的后端，前端https://github.com/socketio/socket.io/ ）
* WebSocket小蝌蚪 Demo：http://www.generallycloud.com/web-socket/rumpetroll/index.html                                
 （我写的后端，前端https://github.com/danielmahal/Rumpetroll ）
