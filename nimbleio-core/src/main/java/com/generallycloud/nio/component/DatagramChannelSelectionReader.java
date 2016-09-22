package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

import com.generallycloud.nio.acceptor.DatagramChannelFactory;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.protocol.DatagramPacket;

public class DatagramChannelSelectionReader implements SelectionAcceptor {

	private NIOContext	context		;
	private ByteBuffer	cacheBuffer	= ByteBuffer.allocate(DatagramPacket.PACKET_MAX);
	private Logger		logger		= LoggerFactory.getLogger(DatagramChannelSelectionReader.class);

	public DatagramChannelSelectionReader(NIOContext context) {
		this.context = context;
	}

	public void accept(SelectionKey selectionKey) throws IOException {

		NIOContext context = this.context;

		ByteBuffer cacheBuffer = this.cacheBuffer;

		cacheBuffer.clear();

		DatagramChannel channel = (DatagramChannel) selectionKey.channel();

		InetSocketAddress remoteSocketAddress = (InetSocketAddress) channel.receive(cacheBuffer);

		DatagramChannelFactory factory = context.getDatagramChannelFactory();

		DatagramPacket packet = new DatagramPacket(cacheBuffer, remoteSocketAddress);

		DatagramPacketAcceptor acceptor = context.getDatagramPacketAcceptor();
		
		if (acceptor == null) {
			logger.debug("______________ none acceptor for context");
			return;
		}

		com.generallycloud.nio.component.DatagramChannel datagramChannel = factory.getDatagramChannel(context, selectionKey, remoteSocketAddress);

		acceptor.accept(datagramChannel, packet);

	}
}
