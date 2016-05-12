package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.component.protocol.udp.DatagramPacket;
import com.gifisan.nio.server.NIOContext;

public class UDPSelectionReader implements SelectionAcceptor {

	private NIOContext	context		= null;
	private ByteBuffer	cacheBuffer	= ByteBuffer.allocate(1500 - 20 - 8);

	public UDPSelectionReader(NIOContext context) {
		this.context = context;
	}

	public void accept(SelectionKey selectionKey) throws IOException {

		NIOContext context = this.context;

		UDPEndPointFactory factory = context.getUDPEndPointFactory();

		UDPEndPoint endPoint = factory.getUDPEndPoint(context, selectionKey);

		if (endPoint.isEndConnect()) {
			return;
		}

		DatagramPacket packet = endPoint.readPacket(cacheBuffer);

		DatagramPacketAcceptor acceptor = context.getDatagramPacketAcceptor();

		acceptor.accept(endPoint, packet);

	}
}
