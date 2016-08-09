package com.gifisan.nio.acceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;

import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.DefaultEndPointWriter;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.SelectorLoop;
import com.gifisan.nio.component.concurrent.FixedAtomicInteger;
import com.gifisan.nio.component.concurrent.UniqueThread;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public final class TCPAcceptor extends AbstractIOAcceptor {

	private EndPointWriter		endPointWriter			;
	private UniqueThread		endPointWriterThread	;
	private SelectorLoop []		selectorLoops			;
	private UniqueThread []		selectorLoopThreads		;
	private ServerSocketChannel	channel				;
	private ServerSocket		serverSocket			;
	

	protected void bind(InetSocketAddress socketAddress) throws IOException {
		
		// 打开服务器套接字通道
		this.channel = ServerSocketChannel.open();
		// 服务器配置为非阻塞
		this.channel.configureBlocking(false);
		// 检索与此通道关联的服务器套接字
		this.serverSocket = channel.socket();
		// 进行服务的绑定
		this.serverSocket.bind(socketAddress, 50);
		
		NIOContext context = this.context;
		
		ServerConfiguration configuration = context.getServerConfiguration();
		
		int core_size = configuration.getSERVER_CORE_SIZE();
		
		CoreProcessors processors = new CoreProcessors(core_size);
		
		this.endPointWriter = new DefaultEndPointWriter(configuration.getSERVER_WRITE_QUEUE_SIZE());
		
		this.selectorLoops = new SelectorLoop[core_size];
		
		for (int i = 0; i < core_size; i++) {
			selectorLoops[i] = new ServerTCPSelectorLoop(context, endPointWriter,processors);
		}
		
		for (int i = 0; i < core_size; i++) {
			selectorLoops[i].register(context, channel);
		}
	}

	protected void startComponent(NIOContext context) {
		
		ServerConfiguration configuration = context.getServerConfiguration();
		
		this.endPointWriterThread = new UniqueThread(endPointWriter, endPointWriter.toString());
		
		this.endPointWriterThread.start();

		int core_size = configuration.getSERVER_CORE_SIZE();
		
		selectorLoopThreads = new UniqueThread[core_size];
		
		for (int i = 0; i < core_size; i++) {
			
			SelectorLoop selectorLoop = selectorLoops[i];
			
			selectorLoopThreads[i] = new UniqueThread(selectorLoop, getSelectorDescription());
			
			selectorLoopThreads[i].start();
		}
	}
	
	private String getSelectorDescription(){
		return "TCP:Selector@edp" + serverSocket.getLocalSocketAddress();
	}

	protected void stopComponent(NIOContext context) {
		
		ServerConfiguration configuration = context.getServerConfiguration();
		
		int core_size = configuration.getSERVER_CORE_SIZE();
		
		for (int i = 0; i < core_size; i++) {
			
			LifeCycleUtil.stop(selectorLoopThreads[i]);
		}
		
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

}
