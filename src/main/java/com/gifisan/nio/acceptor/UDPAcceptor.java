package com.gifisan.nio.acceptor;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.UDPSelectorLoop;
import com.gifisan.nio.component.concurrent.UniqueThread;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public final class UDPAcceptor extends AbstractIOAcceptor {

	private DatagramChannel	channel			;
	private DatagramSocket	serverSocket		;
	private UDPSelectorLoop	selectorLoop		;
	private Selector		selector			;
	private UniqueThread	selectorLoopThread	= new UniqueThread();

	protected UDPAcceptor(NIOContext context) {
		this.context = context;
	}

	protected void bind(InetSocketAddress socketAddress) throws IOException {
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

	protected void startComponent(NIOContext context, Selector selector) {

		this.selectorLoop = new UDPSelectorLoop(context, selector);

		this.selectorLoopThread.start(selectorLoop, selectorLoop.toString());
	}

	protected void stopComponent(NIOContext context, Selector selector) {

		if (channel.isOpen()) {
			CloseUtil.close(channel);
		}

		LifeCycleUtil.stop(selectorLoopThread);
	}
	
	protected void setIOService(NIOContext context) {
		context.setUDPService(this);
	}

	protected int getSERVER_PORT(ServerConfiguration configuration) {

		int SERVER_PORT = configuration.getSERVER_UDP_PORT();

		if (SERVER_PORT < 1) {
			throw new IllegalArgumentException("SERVER.UDP_PORT 参数错误");
		}

		return SERVER_PORT;
	}

	protected UDPSelectorLoop getSelectorLoop() {
		return selectorLoop;
	}

}
