package com.gifisan.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.server.selector.SelectorManagerLoop;

public final class NIOConnector extends AbstractLifeCycle implements Connector {

	private int				port				= 8600;
	private NIOServer			server			= null;
	private ServerSocketChannel	channel			= null;
	private ServerSocket		serverSocket		= null;
	private SelectorManagerLoop	selectorManagerLoop	= null;
	private AtomicBoolean		connected			= new AtomicBoolean(false);

	public NIOConnector(ServerContext context) {
		this.server = context.getServer();
		this.selectorManagerLoop = new SelectorManagerLoop(context);
	}

	private InetSocketAddress getInetSocketAddress() {
		return new InetSocketAddress(this.port);
	}

	public void connect() throws IOException {
		if (connected.compareAndSet(false, true)) {
			// 打开服务器套接字通道
			channel = ServerSocketChannel.open();
			// 服务器配置为非阻塞
			channel.configureBlocking(false);
			// 检索与此通道关联的服务器套接字
			serverSocket = channel.socket();
			// localPort = serverSocket.getLocalPort();
			// 进行服务的绑定
			serverSocket.bind(getInetSocketAddress(),50);
		}
	}
	
	public void close() throws IOException {
		if (connected.compareAndSet(true, false)) {
			if (channel.isOpen()) {
				channel.close();
			}
		}
	}

	public int getPort() {
		return this.port;
	}

	public NIOServer getServer() {
		return this.server;
	}

	protected void doStart() throws Exception {
		if (this.server == null) {
			throw new IllegalStateException("No server");
		}

		this.connect();

		this.selectorManagerLoop.register(channel);

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
