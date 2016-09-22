package com.generallycloud.nio.acceptor;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.DatagramChannelSelectorLoop;
import com.generallycloud.nio.component.concurrent.EventLoopThread;
import com.generallycloud.nio.configuration.ServerConfiguration;

public final class DatagramChannelAcceptor extends AbstractIOAcceptor {
	
	private DatagramChannelSelectorLoop		selectorLoop		;
	private EventLoopThread		selectorLoopThread	;
	private DatagramChannel		channel			;
	private DatagramSocket		serverSocket		;

	protected void bind(NIOContext context,InetSocketAddress socketAddress) throws IOException {
		
		// 打开服务器套接字通道
		this.channel = DatagramChannel.open();
		// 服务器配置为非阻塞
		this.channel.configureBlocking(false);
		// 检索与此通道关联的服务器套接字
		this.serverSocket = channel.socket();
		// 进行服务的绑定
		this.serverSocket.bind(socketAddress);
		
		this.selectorLoop = new ServerUDPSelectorLoop(context);
		
		this.selectorLoop.register(context, channel);
		
		this.selectorLoopThread = new EventLoopThread(selectorLoop, getServiceDescription()+"(selector)");

		this.selectorLoopThread.start();
	}
	
	public String getServiceDescription() {
		return "UDP:" + getServerSocketAddress();
	}

	public InetSocketAddress getServerSocketAddress() {
		return (InetSocketAddress) serverSocket.getLocalSocketAddress();
	}

	protected void unbind(NIOContext context) {

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

}
