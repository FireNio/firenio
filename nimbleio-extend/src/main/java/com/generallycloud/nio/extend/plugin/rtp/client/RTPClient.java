package com.generallycloud.nio.extend.plugin.rtp.client;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.DisconnectException;
import com.generallycloud.nio.common.ByteUtil;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.OnReadFuture;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.concurrent.Waiter;
import com.generallycloud.nio.component.protocol.DatagramPacket;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;
import com.generallycloud.nio.connector.DatagramChannelConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.plugin.jms.MQException;
import com.generallycloud.nio.extend.plugin.jms.MapMessage;
import com.generallycloud.nio.extend.plugin.jms.client.MessageProducer;
import com.generallycloud.nio.extend.plugin.jms.client.impl.DefaultMessageProducer;
import com.generallycloud.nio.extend.plugin.jms.client.impl.FixedMessageConsumer;
import com.generallycloud.nio.extend.plugin.jms.client.impl.OnMappedMessage;
import com.generallycloud.nio.extend.plugin.rtp.RTPException;
import com.generallycloud.nio.extend.plugin.rtp.server.RTPCreateRoomServlet;
import com.generallycloud.nio.extend.plugin.rtp.server.RTPJoinRoomServlet;
import com.generallycloud.nio.extend.plugin.rtp.server.RTPServerDPAcceptor;
import com.generallycloud.nio.extend.security.Authority;

public class RTPClient {

	public static final String	CURRENT_MARK	= "CURRENT_MARK";
	public static final String	GROUP_SIZE	= "GROUP_SIZE";
	public static final String	MARK_INTERVAL	= "MARK_INTERVAL";

	private DatagramChannelConnector		connector		;
	private FixedMessageConsumer	consumer		;
	private NIOContext			context		;
	private String				inviteUsername	;
	private MessageProducer		producer		;
	private String				roomID		;
	private FixedSession		session		;
	private RTPHandle			handle		;

	public RTPClient(FixedSession session, DatagramChannelConnector connector) {
		this(session, connector, new FixedMessageConsumer(session), new DefaultMessageProducer(session));
	}

	// FIXME listen onf break
	public RTPClient(FixedSession session, DatagramChannelConnector connector, FixedMessageConsumer consumer,
			MessageProducer producer) {
		this.connector = connector;
		this.session = session;
		this.producer = producer;
		this.consumer = consumer;
		this.context = connector.getContext();
	}

	public void setRTPHandle(final RTPHandle handle) throws RTPException {

		if (this.handle != null) {
			return;
		}

		this.consumer.listen("invite", new OnMappedMessage() {

			public void onReceive(MapMessage message) {
				handle.onInvite(RTPClient.this, message);
			}
		});

		this.consumer.listen("invite-reply", new OnMappedMessage() {

			public void onReceive(MapMessage message) {
				handle.onInviteReplyed(RTPClient.this, message);
			}
		});

		this.consumer.listen("break", new OnMappedMessage() {

			public void onReceive(MapMessage message) {
				handle.onBreak(RTPClient.this, message);
			}
		});

		this.handle = handle;

		try {

			this.consumer.receive(null);
		} catch (MQException e) {
			throw new RTPException(e);
		}
	}

	public RTPHandle getRTPHandle() {
		return handle;
	}

	public boolean createRoom(String inviteUsername) throws RTPException {

		NIOReadFuture future;

		try {
			future = session.request(RTPCreateRoomServlet.SERVICE_NAME, null);
		} catch (IOException e) {
			throw new RTPException(e.getMessage(), e);
		}

		String roomID = future.getText();

		if ("-1".equals(roomID)) {
			throw new RTPException("create room failed");
		}

		this.roomID = roomID;

		this.inviteCustomer(inviteUsername);

		return true;
	}

	protected NIOContext getContext() {
		return context;
	}

	public String getInviteUsername() {
		return inviteUsername;
	}

	public void inviteCustomer(String inviteUsername) throws RTPException {

		if (roomID == null) {
			throw new RTPException("none roomID,create room first");
		}
		
		Authority authority = session.getAuthority();
		
		if (authority == null) {
			throw new RTPException("not login");
		}

		MapMessage message = new MapMessage("msgID", inviteUsername);

		message.put("eventName", "invite");
		message.put("roomID", roomID);
		message.put("inviteUsername", authority.getUsername());

		try {
			producer.offer(message);

		} catch (MQException e) {
			throw new RTPException(e);
		}

		this.inviteUsername = inviteUsername;
	}

	public void inviteReply(String inviteUsername, int markinterval, long currentMark, int groupSize)
			throws RTPException {

		MapMessage message = new MapMessage("msgID", inviteUsername);

		message.put("eventName", "invite-reply");
		message.put(MARK_INTERVAL, markinterval);
		message.put(CURRENT_MARK, currentMark);
		message.put(GROUP_SIZE, groupSize);

		try {
			producer.offer(message);
		} catch (MQException e) {
			throw new RTPException(e);
		}

		this.inviteUsername = inviteUsername;
	}

	public boolean joinRoom(String roomID) throws RTPException {
		try {

			NIOReadFuture future = session.request(RTPJoinRoomServlet.SERVICE_NAME, roomID);

			return ByteUtil.isTrue(future.getText());
		} catch (IOException e) {
			throw new RTPException(e.getMessage(), e);
		}
	}

	public boolean leaveRoom() throws RTPException {
		try {
			
			Authority authority = session.getAuthority();
			
			if (authority == null) {
				throw new RTPException("not login");
			}

			NIOReadFuture future = session.request(RTPJoinRoomServlet.SERVICE_NAME, roomID);

			this.handle.onBreak(this, new MapMessage("", authority.getUuid()));

			return ByteUtil.isTrue(future.getText());
		} catch (IOException e) {
			throw new RTPException(e.getMessage(), e);
		}
	}

	public void sendDatagramPacket(DatagramPacket packet) throws RTPException {

		if (roomID == null) {
			throw new RTPException("none roomID,create room first");
		}

		connector.sendDatagramPacket(packet);
	}

	public void setRoomID(String roomID) {
		this.roomID = roomID;
	}

	public String getRoomID() {
		return roomID;
	}

	public void setRTPClientDPAcceptor(RTPClientDPAcceptor acceptor) {
		context.setDatagramPacketAcceptor(acceptor);
	}
	
	public void bindTCPSession() throws IOException {

		if (connector == null) {
			throw new IllegalArgumentException("null udp connector");
		}
		
		Authority authority = session.getAuthority();

		if (authority == null) {
			throw new IllegalArgumentException("not login");
		}

		JSONObject json = new JSONObject();

		json.put("serviceName", RTPServerDPAcceptor.BIND_SESSION);

		json.put("username", authority.getUsername());
		json.put("password", authority.getPassword());

		final DatagramPacket packet = new DatagramPacket(json.toJSONString().getBytes(context.getEncoding()));

		final String BIND_SESSION_CALLBACK = RTPServerDPAcceptor.BIND_SESSION_CALLBACK;

		final Waiter<Integer> waiter = new Waiter<Integer>();

		session.listen(BIND_SESSION_CALLBACK, new OnReadFuture() {

			public void onResponse(Session session, ReadFuture future) {

				waiter.setPayload(0);
			}
		});

		ThreadUtil.execute(new Runnable() {

			public void run() {
				
				for (int i = 0; i < 10; i++) {

					connector.sendDatagramPacket(packet);

					if (waiter.wait4Callback(300)) {

						break;
					}
				}
			}
		});

		if (waiter.await(3000)) {
			CloseUtil.close(connector);

			throw new DisconnectException("disconnected");
		}
	}
}
