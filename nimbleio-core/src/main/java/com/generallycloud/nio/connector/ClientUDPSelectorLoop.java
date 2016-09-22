package com.generallycloud.nio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.generallycloud.nio.acceptor.UDPEndPointFactory;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.DatagramChannel;
import com.generallycloud.nio.component.UDPSelectorLoop;

public class ClientUDPSelectorLoop extends UDPSelectorLoop {

	private DatagramChannel	datagramChannel;

	public ClientUDPSelectorLoop(NIOContext context) {
		super(context);
	}

	public void register(NIOContext context, SelectableChannel channel) throws IOException {

		channel.configureBlocking(false);

		this.selector = Selector.open();

		SelectionKey selectionKey = channel.register(selector, SelectionKey.OP_READ);

		UDPEndPointFactory endPointFactory = context.getUDPEndPointFactory();

		java.nio.channels.DatagramChannel ch = (java.nio.channels.DatagramChannel) channel;

		InetSocketAddress socketAddress = (InetSocketAddress) ch.socket().getLocalSocketAddress();

		this.datagramChannel = endPointFactory.getUDPEndPoint(context, selectionKey, socketAddress);

	}

	public DatagramChannel getEndPoint() {
		return datagramChannel;
	}

}
