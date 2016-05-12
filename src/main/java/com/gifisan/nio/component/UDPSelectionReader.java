package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.protocol.udp.DatagramPacket;
import com.gifisan.nio.server.NIOContext;
import com.sun.net.ssl.internal.ssl.Debug;

public class UDPSelectionReader implements SelectionAcceptor {

	private NIOContext	context		= null;
	private ByteBuffer	cacheBuffer	= ByteBuffer.allocate(DatagramPacket.PACKET_MAX);

	public UDPSelectionReader(NIOContext context) {
		this.context = context;
	}

	public void accept(SelectionKey selectionKey) throws IOException {

		NIOContext context = this.context;

		UDPEndPointFactory factory = context.getUDPEndPointFactory();

		UDPEndPoint endPoint = factory.getUDPEndPoint(context, selectionKey);
		
		cacheBuffer.clear();

		DatagramPacket packet = endPoint.readPacket(cacheBuffer);

		DatagramPacketAcceptor acceptor = context.getDatagramPacketAcceptor();

		acceptor.accept(endPoint, packet);
		
		DebugUtil.error("========================"+endPoint);
	}
}
