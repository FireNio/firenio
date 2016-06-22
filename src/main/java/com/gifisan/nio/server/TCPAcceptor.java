package com.gifisan.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.IOAcceptor;
import com.gifisan.nio.component.TCPSelectorLoop;
import com.gifisan.nio.concurrent.UniqueThread;
import com.gifisan.nio.server.configuration.ServerConfiguration;

public final class TCPAcceptor implements IOAcceptor {

	private ServerSocketChannel	channel				= null;
	private ServerSocket		serverSocket			= null;
	private TCPSelectorLoop		selectorLoop			= null;
	private Selector			selector				= null;
	private NIOContext			context				= null;
	private EndPointWriter		endPointWriter			= null;
	private AtomicBoolean		connected				= new AtomicBoolean(false);
	private UniqueThread		endPointWriterThread	= new UniqueThread();
	private UniqueThread		selectorLoopThread		= new UniqueThread();

	public TCPAcceptor(NIOContext context) {
		this.context = context;
	}

	private void bind(InetSocketAddress socketAddress) throws IOException {
		// 打开服务器套接字通道
		this.channel = ServerSocketChannel.open();
		// 服务器配置为非阻塞
		this.channel.configureBlocking(false);
		// 检索与此通道关联的服务器套接字
		this.serverSocket = channel.socket();
		// 进行服务的绑定
		this.serverSocket.bind(socketAddress, 50);
		// 打开selector
		this.selector = Selector.open();
		// 注册监听事件到该selector
		this.channel.register(selector, SelectionKey.OP_ACCEPT);

	}

	public void bind() throws IOException {

		if (connected.compareAndSet(false, true)) {
			
			ServerConfiguration configuration = context.getServerConfiguration();

			int SERVER_PORT = configuration.getSERVER_TCP_PORT();

			if (SERVER_PORT < 1) {
				throw new IllegalArgumentException("SERVER.TCP_PORT 参数错误");
			}
			
			this.bind(getInetSocketAddress(SERVER_PORT));

			this.endPointWriter = new ServerEndPointWriter();

			this.selectorLoop = new TCPSelectorLoop(context, selector, endPointWriter);

			this.endPointWriterThread.start(endPointWriter, endPointWriter.toString());

			this.selectorLoopThread.start(selectorLoop, selectorLoop.toString());
		}
	}

	public void unbind() {
		if (connected.compareAndSet(true, false)) {
			if (channel.isOpen()) {
				CloseUtil.close(channel);
			}

			this.selectorLoopThread.stop();

			this.endPointWriterThread.stop();
		}
	}

	protected TCPSelectorLoop getSelectorManagerLoop() {
		return selectorLoop;
	}
	
	private InetSocketAddress getInetSocketAddress(int port) {
		return new InetSocketAddress(port);
	}

}