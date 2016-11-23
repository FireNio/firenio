package com.generallycloud.nio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.DatagramChannel;
import com.generallycloud.nio.component.concurrent.EventLoopThread;
import com.generallycloud.nio.protocol.DatagramPacket;

public class DatagramChannelConnector extends AbstractChannelConnector {

	private Logger							logger			= LoggerFactory
																.getLogger(DatagramChannelConnector.class);
	private ByteBuffer						cacheBuffer		= ByteBuffer.allocate(DatagramPacket.PACKET_MAX);
	private EventLoopThread					selectorLoopThread	= null;
	private ClientDatagramChannelSelectorLoop	selectorLoop		= null;

	protected EventLoopThread getSelectorLoopThread() {
		return selectorLoopThread;
	}

	public DatagramChannelConnector(BaseContext context) {
		super(context);
	}

	public String getServiceDescription() {
		return "UDP:" + getServerSocketAddress();
	}

	public InetSocketAddress getServerSocketAddress() {
		return datagramChannel().getLocalSocketAddress();
	}

	private DatagramChannel datagramChannel() {
		return selectorLoop.getDatagramChannel();
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

		this.selectableChannel = java.nio.channels.DatagramChannel.open();

		this.selectorLoop = new ClientDatagramChannelSelectorLoop(context, selectableChannel);

		this.selectorLoop.startup();

		((java.nio.channels.DatagramChannel) this.selectableChannel).connect(socketAddress);

		// FIXME rebuild selector
		this.datagramChannel().setSession(session);

		this.selectorLoopThread = new EventLoopThread(selectorLoop, getServiceDescription() + "(selector)");

		this.selectorLoopThread.startup();
	}

	protected void doPhysicalClose0() {

		DatagramChannel datagramChannel = datagramChannel();

		if (datagramChannel != null && datagramChannel.isOpened()) {
			CloseUtil.close(datagramChannel);
		}

		LifeCycleUtil.stop(selectorLoopThread);

		CloseUtil.close(datagramChannel);
	}

}
