package com.gifisan.nio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.DisconnectException;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.MathUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.common.Waiter;
import com.gifisan.nio.component.DatagramPacketAcceptor;
import com.gifisan.nio.component.IOConnector;
import com.gifisan.nio.component.UDPSelectorLoop;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.protocol.DatagramPacket;
import com.gifisan.nio.concurrent.UniqueThread;
import com.gifisan.nio.plugin.rtp.server.RTPServerDPAcceptor;
import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.server.configuration.ServerConfiguration;

public class UDPConnector implements IOConnector {

	private AtomicBoolean		connected			= new AtomicBoolean(false);
	private NIOContext			context			= null;
	private ClientUDPEndPoint	endPoint			= null;
	private Logger				logger			= LoggerFactory.getLogger(UDPConnector.class);
	private Selector			selector			= null;
	private UDPSelectorLoop		selectorLoop		= null;
	private UniqueThread		selectorLoopThread	= new UniqueThread();
	private ByteBuffer			cacheBuffer		= ByteBuffer.allocate(DatagramPacket.PACKET_MAX);
	private InetSocketAddress	serverAddress		= null;
	private ClientSession		session			= null;

	protected UDPSelectorLoop getSelectorLoop() {
		return selectorLoop;
	}

	public UDPConnector(ClientSession session) {
		this.session = session;
		this.context = session.getContext();
	}

	public void close() throws IOException {

		Thread thread = Thread.currentThread();

		if (selectorLoop.isMonitor(thread)) {
			throw new IllegalStateException("not allow to close on future callback");
		}

		if (connected.compareAndSet(true, false)) {

			selectorLoopThread.stop();

			CloseUtil.close(endPoint);

			selector.close();
		}
	}

	public void connect() throws IOException {
		if (connected.compareAndSet(false, true)) {

			this.connect0();
			
			this.selectorLoopThread.start(selectorLoop,selectorLoop.toString());
		}
	}

	private void connect0() throws IOException {
		DatagramChannel channel = DatagramChannel.open();
		channel.configureBlocking(false);
		Selector selector = Selector.open();
		channel.register(selector, SelectionKey.OP_READ);
		channel.connect(getInetSocketAddress());
		this.selector = selector;
		this.endPoint = new ClientUDPEndPoint(session, channel, serverAddress);
		this.selectorLoop = new UDPSelectorLoop(context, selector);
		this.context.setUDPEndPointFactory(new ClientUDPEndPointFactory(endPoint));
	}

	private InetSocketAddress getInetSocketAddress() {
		if (serverAddress == null) {

			ServerConfiguration configuration = context.getServerConfiguration();

			String SERVER_HOST = configuration.getSERVER_HOST();

			int SERVER_PORT = configuration.getSERVER_PORT();

			this.serverAddress = new InetSocketAddress(SERVER_HOST, SERVER_PORT);
		}
		return serverAddress;
	}

	public NIOContext getContext() {
		return context;
	}

	public String toString() {
		return "UDP:Connector@" + endPoint.toString();
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

	public void onDatagramPacketReceived(final DatagramPacketAcceptor receive) {
		((ProtectedClientSession) session).setDatagramPacketAcceptor(receive);
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

	public void bindSession() throws IOException {

		ClientSession session = this.session;

		String sessionID = session.getSessionID();

		JSONObject json = new JSONObject();

		json.put("serviceName", RTPServerDPAcceptor.BIND_SESSION);

		json.put("sessionID", sessionID);

		final DatagramPacket packet = new DatagramPacket(json.toJSONString().getBytes(context.getEncoding()));

		final String BIND_SESSION_CALLBACK = RTPServerDPAcceptor.BIND_SESSION_CALLBACK;

		final CountDownLatch latch = new CountDownLatch(1);

		session.listen(BIND_SESSION_CALLBACK, new OnReadFuture() {

			public void onResponse(ClientSession session, ReadFuture future) {

				latch.countDown();

				session.cancelListen(BIND_SESSION_CALLBACK);
			}
		});

		final Waiter<Integer> waiter = new Waiter<Integer>();

		ThreadUtil.execute(new Runnable() {

			public void run() {
				for (int i = 0; i < 1000000; i++) {

					sendDatagramPacket(packet);

					try {
						if (latch.await(300, TimeUnit.MILLISECONDS)) {

							waiter.setPayload(0);

							break;
						}
					} catch (InterruptedException e) {

						CloseUtil.close(UDPConnector.this);
					}
				}
			}
		});

		if (!waiter.await(3000)) {
			CloseUtil.close(this);

			throw DisconnectException.INSTANCE;
		}
	}
}
