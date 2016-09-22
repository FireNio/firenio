package com.generallycloud.nio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.generallycloud.nio.acceptor.DatagramChannelFactory;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.DatagramChannel;
import com.generallycloud.nio.component.DatagramChannelSelectorLoop;

public class ClientUDPSelectorLoop extends DatagramChannelSelectorLoop {

	private DatagramChannel	datagramChannel;

	public ClientUDPSelectorLoop(NIOContext context) {
		super(context);
	}

	public void register(NIOContext context, SelectableChannel channel) throws IOException {

		channel.configureBlocking(false);

		this.selector = Selector.open();

		SelectionKey selectionKey = channel.register(selector, SelectionKey.OP_READ);

		DatagramChannelFactory factory = context.getDatagramChannelFactory();

		java.nio.channels.DatagramChannel ch = (java.nio.channels.DatagramChannel) channel;

		InetSocketAddress socketAddress = (InetSocketAddress) ch.socket().getLocalSocketAddress();

		this.datagramChannel = factory.getDatagramChannel(context, selectionKey, socketAddress);

	}

	public DatagramChannel getDatagramChannel() {
		return datagramChannel;
	}

}
