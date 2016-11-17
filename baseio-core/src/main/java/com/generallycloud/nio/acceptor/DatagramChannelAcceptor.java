package com.generallycloud.nio.acceptor;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.DatagramChannelSelectorLoop;
import com.generallycloud.nio.component.concurrent.EventLoopThread;

public final class DatagramChannelAcceptor extends AbstractChannelAcceptor {
	
	private DatagramChannelSelectorLoop		selectorLoop		;
	private EventLoopThread					selectorLoopThread	;
	private DatagramSocket					serverSocket		;

	protected void bind(BaseContext context,InetSocketAddress socketAddress) throws IOException {
		
		// 打开服务器套接字通道
		this.selectableChannel = DatagramChannel.open();
		// 服务器配置为非阻塞
		this.selectableChannel.configureBlocking(false);
		// 检索与此通道关联的服务器套接字
		this.serverSocket = ((DatagramChannel) selectableChannel).socket();
		// 进行服务的绑定
		this.serverSocket.bind(socketAddress);
		
		this.selectorLoop = new ServerUDPSelectorLoop(context,selectableChannel);
		
		this.selectorLoop.startup();
		
		this.selectorLoopThread = new EventLoopThread(selectorLoop, getServiceDescription()+"(selector)");

		this.selectorLoopThread.startup();
	}
	
	public String getServiceDescription() {
		return "UDP:" + getServerSocketAddress();
	}

	public InetSocketAddress getServerSocketAddress() {
		return (InetSocketAddress) serverSocket.getLocalSocketAddress();
	}

	protected void unbind(BaseContext context) {

		LifeCycleUtil.stop(selectorLoopThread);
	}

}
