package com.gifisan.nio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.gifisan.nio.acceptor.UDPEndPointFactory;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.UDPEndPoint;
import com.gifisan.nio.component.UDPSelectorLoop;

public class ClientUDPSelectorLoop extends UDPSelectorLoop {

	private UDPEndPoint	endPoint;

	public ClientUDPSelectorLoop(NIOContext context) {
		super(context);
	}

	public void register(NIOContext context, SelectableChannel channel) throws IOException {

		channel.configureBlocking(false);

		this.selector = Selector.open();

		SelectionKey selectionKey = channel.register(selector, SelectionKey.OP_READ);

		UDPEndPointFactory endPointFactory = context.getUDPEndPointFactory();

		DatagramChannel ch = (DatagramChannel) channel;

		InetSocketAddress socketAddress = (InetSocketAddress) ch.socket().getLocalSocketAddress();

		this.endPoint = endPointFactory.getUDPEndPoint(context, selectionKey, socketAddress);

	}

	public UDPEndPoint getEndPoint() {
		return endPoint;
	}

}
