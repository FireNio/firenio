package com.gifisan.nio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.MathUtil;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.UDPSelectorLoop;
import com.gifisan.nio.component.concurrent.UniqueThread;
import com.gifisan.nio.component.protocol.DatagramPacket;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public class UDPConnector extends AbstractIOConnector {

	private ClientUDPEndPoint	endPoint			;
	private Logger				logger			= LoggerFactory.getLogger(UDPConnector.class);
	private UDPSelectorLoop		selectorLoop		;
	private UniqueThread		selectorLoopThread	= new UniqueThread();
	private ByteBuffer			cacheBuffer		= ByteBuffer.allocate(DatagramPacket.PACKET_MAX);

	protected UDPSelectorLoop getSelectorLoop() {
		return selectorLoop;
	}

	public UDPConnector(Session session) {
		this.session = session;
		this.context = session.getContext();
	}

	public String toString() {
		return "UDP:Selector@edp" + this.localAddress.toString();
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

	protected void connect(InetSocketAddress address) throws IOException {
		DatagramChannel channel = DatagramChannel.open();
		channel.configureBlocking(false);
		Selector selector = Selector.open();
		channel.register(selector, SelectionKey.OP_READ);
		channel.connect(address);
		this.selector = selector;
		this.endPoint = new ClientUDPEndPoint(session, channel, address);
		this.selectorLoop = new UDPSelectorLoop(context, selector);
		this.context.setUDPEndPointFactory(new ClientUDPEndPointFactory(endPoint));

	}

	protected void startComponent(NIOContext context, Selector selector) throws IOException {
		this.selectorLoopThread.start(selectorLoop, this.toString());
	}

	protected void stopComponent(NIOContext context, Selector selector) {

		LifeCycleUtil.stop(selectorLoopThread);
		
		CloseUtil.close(endPoint);
	}

	protected int getSERVER_PORT(ServerConfiguration configuration) {
		return configuration.getSERVER_UDP_PORT();
	}

}
