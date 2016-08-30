package com.gifisan.nio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.MathUtil;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.ServerConfiguration;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.UDPEndPoint;
import com.gifisan.nio.component.concurrent.UniqueThread;
import com.gifisan.nio.component.protocol.DatagramPacket;

public class UDPConnector extends AbstractIOConnector {

	private UDPEndPoint			endPoint;
	private Logger				logger		= LoggerFactory.getLogger(UDPConnector.class);
	private ClientUDPSelectorLoop	selectorLoop;
	private UniqueThread		selectorLoopThread;
	private DatagramChannel		channel;
	private ByteBuffer			cacheBuffer	= ByteBuffer.allocate(DatagramPacket.PACKET_MAX);

	protected UniqueThread getSelectorLoopThread() {
		return selectorLoopThread;
	}

	public UDPConnector(Session session) {
		this.session = session;
		this.context = session.getContext();
	}

	public String getServiceDescription() {
		return "UDP:" + getServerSocketAddress();
	}

	public InetSocketAddress getServerSocketAddress() {
		return endPoint.getLocalSocketAddress();
	}

	public void sendDatagramPacket(DatagramPacket packet) {

		allocate(cacheBuffer, packet);

		try {
			endPoint.sendPacket(cacheBuffer, serverAddress);
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

		this.channel = DatagramChannel.open();

		this.selectorLoop = new ClientUDPSelectorLoop(context);

		this.selectorLoop.register(context, channel);

		this.channel.connect(socketAddress);

		this.endPoint = selectorLoop.getEndPoint();

		this.endPoint.setSession(session);

		this.selectorLoopThread = new UniqueThread(selectorLoop, getServiceDescription() + "(Selector)");

		this.selectorLoopThread.start();
	}

	protected void close(NIOContext context) {

		if (channel != null && channel.isConnected()) {
			CloseUtil.close(channel);
		}

		LifeCycleUtil.stop(selectorLoopThread);

		CloseUtil.close(endPoint);
	}

	protected int getSERVER_PORT(ServerConfiguration configuration) {
		return configuration.getSERVER_UDP_PORT();
	}

}
