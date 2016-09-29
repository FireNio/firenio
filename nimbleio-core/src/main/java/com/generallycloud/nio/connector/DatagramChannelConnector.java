package com.generallycloud.nio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.DatagramChannel;
import com.generallycloud.nio.component.concurrent.EventLoopThread;
import com.generallycloud.nio.component.protocol.DatagramPacket;
import com.generallycloud.nio.configuration.ServerConfiguration;

public class DatagramChannelConnector extends AbstractIOConnector {

	private DatagramChannel		datagramChannel;
	private Logger				logger		= LoggerFactory.getLogger(DatagramChannelConnector.class);
	private ByteBuffer			cacheBuffer	= ByteBuffer.allocate(DatagramPacket.PACKET_MAX);
	private ClientUDPSelectorLoop	selectorLoop;
	private EventLoopThread		selectorLoopThread;

	protected EventLoopThread getSelectorLoopThread() {
		return selectorLoopThread;
	}

	public DatagramChannelConnector(Session session) {
		this.session = session;
		this.context = session.getContext();
	}

	public String getServiceDescription() {
		return "UDP:" + getServerSocketAddress();
	}

	public InetSocketAddress getServerSocketAddress() {
		return datagramChannel.getLocalSocketAddress();
	}

	public void sendDatagramPacket(DatagramPacket packet) {

		allocate(cacheBuffer, packet);

		try {
			datagramChannel.sendPacket(cacheBuffer, serverAddress);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);

			// FIXME close connector
		}
	}

	protected void setIOService(NIOContext context) {
		context.setUDPService(this);
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

	protected void connect(NIOContext context, InetSocketAddress socketAddress) throws IOException {

		java.nio.channels.DatagramChannel datagramChannel = java.nio.channels.DatagramChannel.open();

		this.selectorLoop = new ClientUDPSelectorLoop(context);

		this.selectorLoop.register(context, datagramChannel);

		datagramChannel.connect(socketAddress);

		this.datagramChannel = selectorLoop.getDatagramChannel();

		this.datagramChannel.setSession(session);

		this.selectorLoopThread = new EventLoopThread(selectorLoop, getServiceDescription() + "(selector)");

		this.selectorLoopThread.start();
	}

	protected void doClose() {

		if (datagramChannel != null && datagramChannel.isOpened()) {
			CloseUtil.close(datagramChannel);
		}

		LifeCycleUtil.stop(selectorLoopThread);

		CloseUtil.close(datagramChannel);
	}

	protected int getSERVER_PORT(ServerConfiguration configuration) {
		return configuration.getSERVER_UDP_PORT();
	}

}
