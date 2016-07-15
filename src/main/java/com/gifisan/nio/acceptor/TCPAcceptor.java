package com.gifisan.nio.acceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.DefaultEndPointWriter;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.TCPSelectorLoop;
import com.gifisan.nio.component.concurrent.UniqueThread;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public final class TCPAcceptor extends AbstractIOAcceptor {

	private ServerSocketChannel	channel				;
	private ServerSocket		serverSocket			;
	private TCPSelectorLoop		selectorLoop			;
	private EndPointWriter		endPointWriter			;
	private UniqueThread		endPointWriterThread	= new UniqueThread();
	private UniqueThread		selectorLoopThread		= new UniqueThread();

	protected void bind(InetSocketAddress socketAddress) throws IOException {
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

	protected void startComponent(NIOContext context, Selector selector) {
		
		ServerConfiguration configuration = context.getServerConfiguration();
		
		this.endPointWriter = new DefaultEndPointWriter(configuration.getSERVER_WRITE_QUEUE_SIZE());

		this.selectorLoop = new ServerTCPSelectorLoop(context, selector, endPointWriter);

		this.endPointWriterThread.start(endPointWriter, endPointWriter.toString());

		this.selectorLoopThread.start(selectorLoop, getSelectorDescription());
	}
	
	private String getSelectorDescription(){
		return "TCP:Selector@edp" + serverSocket.getLocalSocketAddress();
	}

	protected void stopComponent(NIOContext context, Selector selector) {
		
		if (channel.isOpen()) {
			CloseUtil.close(channel);
		}
		
		LifeCycleUtil.stop(selectorLoopThread);
		LifeCycleUtil.stop(endPointWriterThread);
	}
	
	protected void setIOService(NIOContext context) {
		context.setTCPService(this);
	}

	protected int getSERVER_PORT(ServerConfiguration configuration) {
		
		int SERVER_PORT = configuration.getSERVER_TCP_PORT();

		if (SERVER_PORT < 1) {
			throw new IllegalArgumentException("SERVER.TCP_PORT 参数错误");
		}
		
		return SERVER_PORT;
	}

	protected TCPSelectorLoop getSelectorManagerLoop() {
		return selectorLoop;
	}
	
}
