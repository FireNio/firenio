package com.generallycloud.nio.acceptor;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.DatagramChannelSelectorLoop;
import com.generallycloud.nio.component.SelectorLoop;

public final class DatagramChannelAcceptor extends AbstractChannelAcceptor{

	public DatagramChannelAcceptor(BaseContext context) {
		super(context);
	}

	protected void initselectableChannel() throws IOException {
		// 打开服务器套接字通道
		this.selectableChannel = ServerSocketChannel.open();
		// 服务器配置为非阻塞
		this.selectableChannel.configureBlocking(false);
		// 检索与此通道关联的服务器套接字
		this.serverSocket = ((ServerSocketChannel) selectableChannel).socket();

	}

	protected SelectorLoop newSelectorLoop(SelectorLoop[] selectorLoops) throws IOException {
		return new DatagramChannelSelectorLoop(this, selectorLoops);
	}

	protected void bind(BaseContext context, InetSocketAddress socketAddress) throws IOException {

		try {
			// 进行服务的绑定
			this.serverSocket.bind(socketAddress, 50);
		} catch (BindException e) {
			throw new BindException(e.getMessage() + " at " + socketAddress.getPort());
		}

		initSelectorLoops();
	}

}
