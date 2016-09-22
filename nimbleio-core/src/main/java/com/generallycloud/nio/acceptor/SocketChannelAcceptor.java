package com.generallycloud.nio.acceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;

import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.SelectorLoop;
import com.generallycloud.nio.component.concurrent.EventLoopThread;
import com.generallycloud.nio.configuration.ServerConfiguration;

public final class SocketChannelAcceptor extends AbstractIOAcceptor {

	private SelectorLoop []		selectorLoops			;
	private EventLoopThread []		selectorLoopThreads		;
	private ServerSocketChannel	channel				;
	private ServerSocket		serverSocket			;
	

	protected void bind(NIOContext context,InetSocketAddress socketAddress) throws IOException {
		
		// 打开服务器套接字通道
		this.channel = ServerSocketChannel.open();
		// 服务器配置为非阻塞
		this.channel.configureBlocking(false);
		// 检索与此通道关联的服务器套接字
		this.serverSocket = channel.socket();
		// 进行服务的绑定
		this.serverSocket.bind(socketAddress, 50);
		
		ServerConfiguration configuration = context.getServerConfiguration();
		
		int core_size = configuration.getSERVER_CORE_SIZE();
		
		CoreProcessors processors = new CoreProcessors(core_size);
		
		this.selectorLoops = new SelectorLoop[core_size];
		
		for (int i = 0; i < core_size; i++) {
			selectorLoops[i] = new ServerTCPSelectorLoop(context,processors);
		}
		
		for (int i = 0; i < core_size; i++) {
			selectorLoops[i].register(context, channel);
		}
		
		selectorLoopThreads = new EventLoopThread[core_size];
		
		for (int i = 0; i < core_size; i++) {
			
			SelectorLoop selectorLoop = selectorLoops[i];
			
			selectorLoopThreads[i] = new EventLoopThread(selectorLoop, getServiceDescription() + "(selector)");
			
			selectorLoopThreads[i].start();
		}
	}

	public String getServiceDescription(){
		return "TCP:" + getServerSocketAddress();
	}
	
	public InetSocketAddress getServerSocketAddress(){
		return (InetSocketAddress) serverSocket.getLocalSocketAddress();
	}

	protected void unbind(NIOContext context) {
		
		ServerConfiguration configuration = context.getServerConfiguration();
		
		int core_size = configuration.getSERVER_CORE_SIZE();
		
		for (int i = 0; i < core_size; i++) {
			
			LifeCycleUtil.stop(selectorLoopThreads[i]);
		}
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

}
