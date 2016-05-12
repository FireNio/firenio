package com.gifisan.nio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.MathUtil;
import com.gifisan.nio.component.ClientUDPEndPoint;
import com.gifisan.nio.component.Connector;
import com.gifisan.nio.component.UDPSelectorLoop;
import com.gifisan.nio.component.protocol.udp.DatagramPacket;

public class ClientUDPConnector implements Connector {

	private AtomicBoolean		connected			= new AtomicBoolean(false);
	private ClientContext		context			= null;
	private ClientUDPEndPoint	endPoint			= null;
	private Logger				logger			= LoggerFactory.getLogger(ClientUDPConnector.class);
	private Selector			selector			= null;
	private UDPSelectorLoop		selectorLoop		= null;
	private ByteBuffer			cacheBuffer		= ByteBuffer.allocate(DatagramPacket.PACKET_MAX);
	private InetSocketAddress	serverSocket		= null;
	private String 			sessionID 		= null;
	
	
	protected UDPSelectorLoop getSelectorLoop() {
		return selectorLoop;
	}

	protected ClientUDPConnector(ClientTCPConnector connector,String sessionID) {
		this.context = connector.getContext();
		this.sessionID = sessionID;
	}

	public void close() throws IOException {

		Thread thread = Thread.currentThread();

		if (selectorLoop.isMonitor(thread)) {
			throw new IllegalStateException("not allow to close on future callback");
		}

		if (connected.compareAndSet(true, false)) {
			LifeCycleUtil.stop(selectorLoop);
			
			CloseUtil.close(endPoint);
		}
	}

	public void connect() throws IOException {
		if (connected.compareAndSet(false, true)) {

			this.connect0();

			try {

				this.selectorLoop.start();
			} catch (Exception e) {
				DebugUtil.debug(e);
			}
		}
	}

	private void connect0() throws IOException {
		DatagramChannel channel = DatagramChannel.open();
		channel.configureBlocking(false);
		selector = Selector.open();
		channel.register(selector, SelectionKey.OP_READ);
		channel.connect(getInetSocketAddress());
		this.endPoint = new ClientUDPEndPoint(context, channel);
		this.selectorLoop = new UDPSelectorLoop(context, selector);
	}
	
	private InetSocketAddress getInetSocketAddress() {
		if (serverSocket == null) {
			serverSocket = new InetSocketAddress(context.getServerHost(), context.getServerPort()+1);
		}
		return serverSocket;
	}

	public ClientContext getContext() {
		return context;
	}

	public String getServerHost() {
		return context.getServerHost();
	}

	public int getServerPort() {
		return context.getServerPort();
	}

	public String toString() {
		return "UDP:Connector@" + endPoint.toString();
	}
	
	public void send(DatagramPacket packet) {
		
		allocate(cacheBuffer, packet);
		
		try {
			endPoint.sendPacket(cacheBuffer,serverSocket);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			
			//FIXME close connector
		}
	}
	
	private void allocate(ByteBuffer buffer,DatagramPacket packet) {
		
		buffer.clear();
		
		if (packet.getTimestamp() == 0) {
			allocate(buffer, packet.getData());
			return;
		}
		allocate(
				buffer, 
				packet.getTimestamp(), 
				packet.getSequenceNo(), 
				packet.getTargetEndpointID(), 
				packet.getData());
		
	}
	
	private void allocate(ByteBuffer buffer,long timestamp, int sequenceNO, long targetEndPointID,byte [] data) {
		
		byte [] bytes = buffer.array();
		
		MathUtil.long2Byte(bytes, timestamp, 0);
		MathUtil.int2Byte(bytes, sequenceNO, 8);
		MathUtil.long2Byte(bytes, targetEndPointID, 12);
		
		allocate(buffer, data);
	}
	
	private void allocate(ByteBuffer buffer,byte [] data) {
		buffer.position(DatagramPacket.PACKET_HEADER);
		buffer.put(data);
		buffer.flip();
	}

}
