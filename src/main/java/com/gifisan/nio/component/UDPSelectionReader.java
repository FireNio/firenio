package com.gifisan.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.protocol.DatagramPacket;
import com.gifisan.nio.server.NIOContext;

public class UDPSelectionReader implements SelectionAcceptor {

	private NIOContext	context		= null;
	private ByteBuffer	cacheBuffer	= ByteBuffer.allocate(DatagramPacket.PACKET_MAX);
	private Logger		logger		= LoggerFactory.getLogger(UDPSelectionReader.class);

	public UDPSelectionReader(NIOContext context) {
		this.context = context;
	}

	public void accept(SelectionKey selectionKey) throws IOException {

		NIOContext context = this.context;

		ByteBuffer cacheBuffer = this.cacheBuffer;

		cacheBuffer.clear();

		DatagramChannel channel = (DatagramChannel) selectionKey.channel();

		InetSocketAddress remoteSocketAddress = (InetSocketAddress) channel.receive(cacheBuffer);

		UDPEndPointFactory factory = context.getUDPEndPointFactory();

		DatagramPacket packet = new DatagramPacket(cacheBuffer, remoteSocketAddress);

		DatagramPacketAcceptor acceptor = context.getDatagramPacketAcceptor();
		
		if (acceptor == null) {
			logger.debug("______________ none acceptor for context");
			return;
		}

		UDPEndPoint endPoint = factory.getUDPEndPoint(context, selectionKey, remoteSocketAddress);

		acceptor.accept(endPoint, packet);

	}
}
