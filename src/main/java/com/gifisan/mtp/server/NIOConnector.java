package com.gifisan.mtp.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.server.selector.SelectorManagerTask;

public final class NIOConnector extends AbstractLifeCycle implements Connector {

	private int				port				= 8600;
	private MTPServer			server			= null;
	private ServerSocketChannel	channel			= null;
	private ServerSocket		serverSocket		= null;
	private SelectorManagerTask	selectorManagerTask	= null;
	private String				host				= "127.0.0.1";
	private AtomicBoolean		connected			= new AtomicBoolean(false);

	public NIOConnector(ServletContext context) {
		this.server = context.getServer();
		this.selectorManagerTask = new SelectorManagerTask(context);
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	private InetSocketAddress getInetSocketAddress() {
		if (StringUtil.isNullOrBlank(host)) {
			return new InetSocketAddress(this.port);
		}
		return new InetSocketAddress(this.host, this.port);
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
			serverSocket.bind(getInetSocketAddress());
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

	public MTPServer getServer() {
		return this.server;
	}

	protected void doStart() throws Exception {
		if (this.server == null) {
			throw new IllegalStateException("No server");
		}

		this.connect();

		this.selectorManagerTask.register(channel);

		this.selectorManagerTask.start();

	}

	protected void doStop() throws Exception {

		LifeCycleUtil.stop(selectorManagerTask);

		this.close();

	}

	public void setPort(int port) {
		this.port = port;
	}

}
