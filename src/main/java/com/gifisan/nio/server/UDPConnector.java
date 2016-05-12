package com.gifisan.nio.server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.Connector;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.ServerEndPointWriter;
import com.gifisan.nio.component.UDPSelectorLoop;

public final class UDPConnector extends AbstractLifeCycle implements Connector {

	private int				serverPort		= 0;
	private DatagramChannel		channel			= null;
	private DatagramSocket		serverSocket		= null;
	private UDPSelectorLoop		selectorLoop		= null;
	private Selector			selector			= null;
	private NIOContext			context			= null;
	private EndPointWriter		endPointWriter		= null;
	private AtomicBoolean		connected			= new AtomicBoolean(false);
	

	protected UDPConnector(NIOContext context,int serverPort) {
		this.context = context;
		this.serverPort = serverPort;
	}

	private InetSocketAddress getInetSocketAddress() {
		return new InetSocketAddress(this.serverPort);
	}

	public void connect() throws IOException {
		if (connected.compareAndSet(false, true)) {
			// 打开服务器套接字通道
			
			this.channel = DatagramChannel.open();  
			// 服务器配置为非阻塞
			this.channel.configureBlocking(false);
			// 检索与此通道关联的服务器套接字
			this.serverSocket = channel.socket();
			// 进行服务的绑定
			this.serverSocket.bind(getInetSocketAddress());
			// 打开selector
			this.selector = Selector.open();
			// 注册监听事件到该selector
			this.channel.register(selector, SelectionKey.OP_READ);

		}
	}

	public void close() throws IOException {
		if (connected.compareAndSet(true, false)) {
			if (channel.isOpen()) {
				CloseUtil.close(channel);
			}
		}
	}

	public int getServerPort() {
		return this.serverPort;
	}

	protected void doStart() throws Exception {

		this.connect();
		
		this.endPointWriter = new ServerEndPointWriter();

		this.selectorLoop = new UDPSelectorLoop(context, selector);

		this.endPointWriter.start();
		
		this.selectorLoop.start();
	}

	protected void doStop() throws Exception {

		LifeCycleUtil.stop(selectorLoop);
		
		LifeCycleUtil.stop(endPointWriter);

		this.close();
	}

	protected UDPSelectorLoop getSelectorLoop() {
		return selectorLoop;
	}

}
