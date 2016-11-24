package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

import com.generallycloud.nio.acceptor.DatagramChannelFactory;
import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.protocol.DatagramPacket;

public class DatagramChannelSelectionReader implements SelectionAcceptor {

	private BaseContext		context;
	private SelectorLoop	selectorLoop;
	private ByteBuf		cacheBuffer	= UnpooledByteBufAllocator.getInstance().allocate(DatagramPacket.PACKET_MAX);
	private Logger			logger		= LoggerFactory.getLogger(DatagramChannelSelectionReader.class);

	public DatagramChannelSelectionReader(SelectorLoop selectorLoop) {
		this.selectorLoop = selectorLoop;
		this.context = selectorLoop.getContext();
	}

	public void accept(SelectionKey selectionKey) throws IOException {

		BaseContext context = this.context;

		ByteBuf cacheBuffer = this.cacheBuffer;

		cacheBuffer.clear();

		DatagramChannel channel = (DatagramChannel) selectionKey.channel();

		InetSocketAddress remoteSocketAddress = (InetSocketAddress) channel.receive(cacheBuffer.nioBuffer());

		DatagramChannelFactory factory = context.getDatagramChannelFactory();

		DatagramPacket packet = new DatagramPacket(context,cacheBuffer, remoteSocketAddress);

		DatagramPacketAcceptor acceptor = context.getDatagramPacketAcceptor();

		if (acceptor == null) {
			logger.debug("______________ none acceptor for context");
			return;
		}

		com.generallycloud.nio.component.DatagramChannel datagramChannel = factory.getDatagramChannel(selectorLoop,
				channel, remoteSocketAddress);

		acceptor.accept(datagramChannel, packet);

	}
}
