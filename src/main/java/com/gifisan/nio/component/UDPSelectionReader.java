package com.gifisan.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.protocol.DatagramPacket;
import com.gifisan.nio.server.NIOContext;

public class UDPSelectionReader implements SelectionAcceptor {

	private NIOContext	context		= null;
	private ByteBuffer	cacheBuffer	= ByteBuffer.allocate(DatagramPacket.PACKET_MAX);

	public UDPSelectionReader(NIOContext context) {
		this.context = context;
	}

	public void accept(SelectionKey selectionKey) throws IOException {

		NIOContext context = this.context;
		
		cacheBuffer.clear();

		DatagramChannel channel = (DatagramChannel) selectionKey.channel();
		
		InetSocketAddress remoteSocketAddress = (InetSocketAddress) channel.receive(cacheBuffer);

		UDPEndPointFactory factory = context.getUDPEndPointFactory();

		DatagramPacket packet = new DatagramPacket(cacheBuffer, remoteSocketAddress);

		DatagramPacketAcceptor acceptor = context.getDatagramPacketAcceptor();

		UDPEndPoint endPoint = factory.getUDPEndPoint(context, selectionKey,remoteSocketAddress);
		
		acceptor.accept(endPoint, packet);
		
		DebugUtil.error("========================"+endPoint);
	}
}
