package com.generallycloud.nio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.DatagramChannel;
import com.generallycloud.nio.component.DatagramChannelSelectorLoop;
import com.generallycloud.nio.component.NioDatagramChannel;
import com.generallycloud.nio.component.SelectorLoop;
import com.generallycloud.nio.protocol.DatagramPacket;

public class DatagramChannelConnector extends AbstractChannelConnector {

	private Logger			logger		= LoggerFactory.getLogger(DatagramChannelConnector.class);
	private ByteBuffer		cacheBuffer	= ByteBuffer.allocate(DatagramPacket.PACKET_MAX);
	private DatagramChannel	channel		= null; //FIXME use parent session

	public DatagramChannelConnector(BaseContext context) {
		super(context);
	}

	private DatagramChannel datagramChannel() {
		return channel;
	}

	public void sendDatagramPacket(DatagramPacket packet) {

		allocate(cacheBuffer, packet);

		try {
			datagramChannel().sendPacket(cacheBuffer, serverAddress);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);

			// FIXME close connector
		}
	}

	private void allocate(ByteBuffer buffer, DatagramPacket packet) {

		buffer.clear();

		if (packet.getTimestamp() == -1) {
			allocate(buffer, packet.getData());
			return;
		}

		allocate(buffer, packet.getTimestamp(), packet.getSequenceNo(), packet.getData());
	}

	private void allocate(ByteBuffer buffer, long timestamp, int sequenceNO, byte[] data) {

		byte[] bytes = buffer.array();

		MathUtil.long2Byte(bytes, timestamp, 0);
		MathUtil.int2Byte(bytes, sequenceNO, 8);

		allocate(buffer, data);
	}

	private void allocate(ByteBuffer buffer, byte[] data) {
		buffer.position(DatagramPacket.PACKET_HEADER);
		buffer.put(data);
		buffer.flip();
	}

	protected void connect(BaseContext context, InetSocketAddress socketAddress) throws IOException {
		
		((java.nio.channels.DatagramChannel) this.selectableChannel).connect(socketAddress);

		this.channel = new NioDatagramChannel(selectorLoops[0], (java.nio.channels.DatagramChannel) selectableChannel,
				socketAddress);

		initSelectorLoops();
	}

	protected void initselectableChannel() throws IOException {

		this.selectableChannel = java.nio.channels.DatagramChannel.open();

		this.selectableChannel.configureBlocking(false);
	}

	protected SelectorLoop newSelectorLoop(SelectorLoop[] selectorLoops) throws IOException {
		return new DatagramChannelSelectorLoop(this, selectorLoops);
	}

}
