package com.gifisan.nio.server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.IOAcceptor;
import com.gifisan.nio.component.UDPSelectorLoop;
import com.gifisan.nio.concurrent.UniqueThread;
import com.gifisan.nio.server.configuration.ServerConfiguration;

public final class UDPAcceptor implements IOAcceptor {

	private DatagramChannel	channel			= null;
	private DatagramSocket	serverSocket		= null;
	private UDPSelectorLoop	selectorLoop		= null;
	private Selector		selector			= null;
	private NIOContext		context			= null;
	private AtomicBoolean	connected			= new AtomicBoolean(false);
	private UniqueThread	selectorLoopThread	= new UniqueThread();

	protected UDPAcceptor(NIOContext context) {
		this.context = context;
	}

	private void bind(InetSocketAddress socketAddress) throws IOException {
		// 打开服务器套接字通道
		this.channel = DatagramChannel.open();
		// 服务器配置为非阻塞
		this.channel.configureBlocking(false);
		// 检索与此通道关联的服务器套接字
		this.serverSocket = channel.socket();
		// 进行服务的绑定
		this.serverSocket.bind(socketAddress);
		// 打开selector
		this.selector = Selector.open();
		// 注册监听事件到该selector
		this.channel.register(selector, SelectionKey.OP_READ);
	}

	private InetSocketAddress getInetSocketAddress(int port) {
		return new InetSocketAddress(port);
	}

	public void bind() throws IOException {
		if (connected.compareAndSet(false, true)) {

			ServerConfiguration configuration = context.getServerConfiguration();

			int SERVER_PORT = configuration.getSERVER_UDP_PORT();

			if (SERVER_PORT < 1) {
				throw new IllegalArgumentException("SERVER.UDP_PORT 参数错误");
			}
			
			this.bind(getInetSocketAddress(SERVER_PORT));
			
			this.selectorLoop = new UDPSelectorLoop(context, selector);

			this.selectorLoopThread.start(selectorLoop, selectorLoop.toString());
		}

	}

	public void unbind() {
		if (connected.compareAndSet(true, false)) {
			if (channel.isOpen()) {
				CloseUtil.close(channel);
			}

			this.selectorLoopThread.stop();
		}
	}

	protected UDPSelectorLoop getSelectorLoop() {
		return selectorLoop;
	}

}
