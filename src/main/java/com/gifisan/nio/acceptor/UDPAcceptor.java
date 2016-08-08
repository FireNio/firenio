package com.gifisan.nio.acceptor;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.UDPSelectorLoop;
import com.gifisan.nio.component.concurrent.UniqueThread;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public final class UDPAcceptor extends AbstractIOAcceptor {
	
	private UDPSelectorLoop	selectorLoop		;
	private UniqueThread	selectorLoopThread	;
	private DatagramChannel		channel		;
	private DatagramSocket		serverSocket	;

	protected void bind(InetSocketAddress socketAddress) throws IOException {
		
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
	}

	protected void startComponent(NIOContext context) {

		this.selectorLoopThread = new UniqueThread(selectorLoop, getSelectorDescription());

		this.selectorLoopThread.start();
	}
	
	private String getSelectorDescription(){
		return "UDP:Selector@edp" + serverSocket.getLocalSocketAddress();
	}
	
	protected void stopComponent(NIOContext context) {

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
