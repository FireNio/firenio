package com.gifisan.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.SelectorManagerLoop;

public final class NIOConnector extends AbstractLifeCycle implements Connector {

	private int				port				= 8600;
	private ServerSocketChannel	channel			= null;
	private ServerSocket		serverSocket		= null;
	private SelectorManagerLoop	selectorManagerLoop	= null;
	private AtomicBoolean		connected			= new AtomicBoolean(false);
	private Selector			selector			= null;
	private NIOContext			context			= null;

	protected NIOConnector(NIOContext context) {
		this.context = context;
	}

	private InetSocketAddress getInetSocketAddress() {
		return new InetSocketAddress(this.port);
	}

	public void connect() throws IOException {
		if (connected.compareAndSet(false, true)) {
			// 打开服务器套接字通道
			this.channel = ServerSocketChannel.open();
			// 服务器配置为非阻塞
			this.channel.configureBlocking(false);
			// 检索与此通道关联的服务器套接字
			this.serverSocket = channel.socket();
			// localPort = serverSocket.getLocalPort();
			// 进行服务的绑定
			this.serverSocket.bind(getInetSocketAddress(), 50);
			// 打开selector
			this.selector = Selector.open();
			// 注册监听事件到该selector
			this.channel.register(selector, SelectionKey.OP_ACCEPT);

		}
	}

	public void close() throws IOException {
		if (connected.compareAndSet(true, false)) {
			if (channel.isOpen()) {
				CloseUtil.close(this.channel);
			}
		}
	}

	public int getPort() {
		return this.port;
	}

	protected void doStart() throws Exception {

		this.connect();

		this.selectorManagerLoop = new SelectorManagerLoop(context, selector);

		this.selectorManagerLoop.start();

	}

	protected void doStop() throws Exception {

		LifeCycleUtil.stop(selectorManagerLoop);

		this.close();

	}

	public void setPort(int port) {
		this.port = port;
	}

}
