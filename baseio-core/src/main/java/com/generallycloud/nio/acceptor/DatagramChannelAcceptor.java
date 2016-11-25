package com.generallycloud.nio.acceptor;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

import com.generallycloud.nio.component.DatagramChannelContext;
import com.generallycloud.nio.component.DatagramChannelSelectorLoop;
import com.generallycloud.nio.component.SelectorLoop;
import com.generallycloud.nio.protocol.ReadFuture;

public final class DatagramChannelAcceptor extends AbstractChannelAcceptor {

	private DatagramChannelContext	context		= null;

	private DatagramSocket			datagramSocket	= null;

	public DatagramChannelAcceptor(DatagramChannelContext context) {
		this.context = context;
	}

	protected void bind(InetSocketAddress socketAddress) throws IOException {

		try {
			// 进行服务的绑定
			datagramSocket.bind(socketAddress);
		} catch (BindException e) {
			throw new BindException(e.getMessage() + " at " + socketAddress.getPort());
		}

		initSelectorLoops();
	}

	public void broadcast(final ReadFuture future) {
		throw new UnsupportedOperationException();
	}

	public DatagramChannelContext getContext() {
		return context;
	}

	protected void initselectableChannel() throws IOException {
		// 打开服务器套接字通道
		this.selectableChannel = DatagramChannel.open();
		// 服务器配置为非阻塞
		this.selectableChannel.configureBlocking(false);

		this.datagramSocket = ((DatagramChannel) this.selectableChannel).socket();
	}

	protected SelectorLoop newSelectorLoop(SelectorLoop[] selectorLoops) throws IOException {
		return new DatagramChannelSelectorLoop(this, selectorLoops);
	}

}
