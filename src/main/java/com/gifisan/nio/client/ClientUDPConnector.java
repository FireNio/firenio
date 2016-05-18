package com.gifisan.nio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.DisconnectException;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.MathUtil;
import com.gifisan.nio.component.ClientUDPEndPoint;
import com.gifisan.nio.component.Connector;
import com.gifisan.nio.component.DatagramPacketAcceptor;
import com.gifisan.nio.component.UDPSelectorLoop;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.protocol.DatagramPacket;
import com.gifisan.nio.plugin.rtp.server.RTPServerDPAcceptor;

public class ClientUDPConnector implements Connector {

	private AtomicBoolean		connected			= new AtomicBoolean(false);
	private ClientContext		context			= null;
	private ClientUDPEndPoint	endPoint			= null;
	private Logger				logger			= LoggerFactory.getLogger(ClientUDPConnector.class);
	private Selector			selector			= null;
	private UDPSelectorLoop		selectorLoop		= null;
	private ByteBuffer			cacheBuffer		= ByteBuffer.allocate(DatagramPacket.PACKET_MAX);
	private InetSocketAddress	serverSocket		= null;
	private ClientSession		session			= null;
	
	protected UDPSelectorLoop getSelectorLoop() {
		return selectorLoop;
	}

	public ClientUDPConnector(ClientSession session) throws Exception {
		this.session = session;
		this.context = session.getContext();
		this.bindSession();
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
				logger.debug(e);
			}
		}
	}

	private void connect0() throws IOException {
		DatagramChannel channel = DatagramChannel.open();
		channel.configureBlocking(false);
		selector = Selector.open();
		channel.register(selector, SelectionKey.OP_READ);
		channel.connect(getInetSocketAddress());
		this.endPoint = new ClientUDPEndPoint(session, channel,serverSocket);
		this.selectorLoop = new UDPSelectorLoop(context, selector);
		this.context.setUDPEndPointFactory(new ClientUDPEndPointFactory(endPoint));
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
	
	public void sendDatagramPacket(DatagramPacket packet) {
		
		allocate(cacheBuffer, packet);
		
		try {
			endPoint.sendPacket(cacheBuffer,serverSocket);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			
			//FIXME close connector
		}
	}
	
	public void onDatagramPacketReceived(final DatagramPacketAcceptor receive){
		((ProtectedClientSession)session).setDatagramPacketAcceptor(receive);
	}
	
	private void allocate(ByteBuffer buffer,DatagramPacket packet) {
		
		buffer.clear();
		
		if (packet.getTimestamp() == -1) {
			allocate(buffer, packet.getData());
			return;
		}
		allocate(
				buffer, 
				packet.getTimestamp(), 
				packet.getSequenceNo(), 
				packet.getRoomID(), 
				packet.getData());
		
	}
	
	private void allocate(ByteBuffer buffer,long timestamp, int sequenceNO, int roomID,byte [] data) {
		
		byte [] bytes = buffer.array();
		
		MathUtil.long2Byte(bytes, timestamp, 0);
		MathUtil.int2Byte(bytes, sequenceNO, 8);
		MathUtil.long2Byte(bytes, roomID, 12);
		
		allocate(buffer, data);
	}
	
	private void allocate(ByteBuffer buffer,byte [] data) {
		buffer.position(DatagramPacket.PACKET_HEADER);
		buffer.put(data);
		buffer.flip();
	}
	
	private void bindSession() throws Exception{
		
		ClientSession session = this.session;
		
		String sessionID = session.getSessionID();
		
		JSONObject json = new JSONObject();
		
		json.put("serviceName", RTPServerDPAcceptor.BIND_SESSION);
		
		json.put("sessionID", sessionID);
		
		DatagramPacket packet = new DatagramPacket(json.toJSONString().getBytes(context.getEncoding()));
		
		final ReentrantLock _lock = new ReentrantLock();
		
		final Condition	called = _lock.newCondition();
		
		final String BIND_SESSION_CALLBACK = RTPServerDPAcceptor.BIND_SESSION_CALLBACK;
		
		final AtomicBoolean atoCalled = new AtomicBoolean();
		
		session.listen(BIND_SESSION_CALLBACK, new OnReadFuture() {
			
			public void onResponse(ClientSession session, ReadFuture future) {
				
				_lock.lock();
				
				called.signal();
				
				atoCalled.compareAndSet(false, true);
				
				_lock.unlock();
				
				session.cancelListen(BIND_SESSION_CALLBACK);
				
			}
		});
		
		this.connect();
		
		for (int i = 0; i < 10; i++) {
			
			this.sendDatagramPacket(packet);
			
			_lock.lock();
			
			try {
				
				if (!atoCalled.get()) {
					called.await(300, TimeUnit.MILLISECONDS);
				}
				
				if (atoCalled.get()) {
					break;
				}
				
			} catch (Exception e) {
				logger.debug(e);
				called.signal();
			}finally{
				
				_lock.unlock();
			}
		}
		
		if (!atoCalled.get()) {
			CloseUtil.close(this);
			
			throw DisconnectException.INSTANCE;
		}
	}
	
}
