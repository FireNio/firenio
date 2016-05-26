package com.gifisan.nio.plugin.rtp.client;

import java.io.Closeable;
import java.io.IOException;

import com.gifisan.nio.client.ClientContext;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.client.ClientUDPConnector;
import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.DatagramPacketAcceptor;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.protocol.DatagramPacket;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.MapMessage;
import com.gifisan.nio.plugin.jms.client.MessageProducer;
import com.gifisan.nio.plugin.jms.client.impl.DefaultMessageProducer;
import com.gifisan.nio.plugin.jms.client.impl.FixedMessageConsumer;
import com.gifisan.nio.plugin.jms.client.impl.OnMappedMessage;
import com.gifisan.nio.plugin.rtp.RTPException;
import com.gifisan.nio.plugin.rtp.server.RTPCreateRoomServlet;
import com.gifisan.nio.plugin.rtp.server.RTPJoinRoomServlet;

public class RTPClient implements Closeable {

	public static final String	CURRENT_MARK	= "CURRENT_MARK";
	public static final String	GROUP_SIZE	= "GROUP_SIZE";
	public static final String	MARK_INTERVAL	= "MARK_INTERVAL";

	private ClientUDPConnector	connector		= null;
	private FixedMessageConsumer	consumer		= null;
	private ClientContext		context		= null;
	private String				inviteUsername	= null;
	private MessageProducer		producer		= null;
	private String				roomID		= null;
	private ClientSession		session		= null;
	
	public RTPClient(ClientSession session,final RTPHandle handle) throws IOException{
		this(session,handle,new FixedMessageConsumer(session),new DefaultMessageProducer(session));
	}

	//FIXME listen onf break
	public RTPClient(ClientSession session,final RTPHandle handle, FixedMessageConsumer consumer, MessageProducer producer)
			throws IOException {
		this.connector = new ClientUDPConnector(session);
		this.session = session;
		this.producer = producer;
		this.consumer = consumer;
		this.context = connector.getContext();

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
		
		try {
			this.consumer.receive(null);
		} catch (JMSException e) {
			throw new RTPException(e.getMessage(),e);
		}
		
	}

	public void close() throws IOException {
		CloseUtil.close(connector);
	}

	public boolean createRoom(String inviteUsername) throws RTPException {

		ReadFuture future;

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

	protected ClientContext getContext() {
		return context;
	}

	public String getInviteUsername() {
		return inviteUsername;
	}

	public void inviteCustomer(String inviteUsername) throws RTPException {

		if (roomID == null) {
			throw new RTPException("none roomID,create room first");
		}

		MapMessage message = new MapMessage("msgID", inviteUsername);

		message.put("eventName", "invite");
		message.put("roomID", roomID);
		message.put("inviteUsername", this.session.getAuthority().getUsername());

		try {
			producer.offer(message);

		} catch (JMSException e) {
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
		} catch (JMSException e) {
			throw new RTPException(e);
		}

		this.inviteUsername = inviteUsername;
	}

	public boolean joinRoom(String roomID) throws RTPException {
		try {

			ReadFuture future = session.request(RTPJoinRoomServlet.SERVICE_NAME, roomID);

			return ByteUtil.isTrue(future.getText());
		} catch (IOException e) {
			throw new RTPException(e.getMessage(), e);
		}
	}

	public void onDatagramPacketReceived(DatagramPacketAcceptor acceptor) {
		connector.onDatagramPacketReceived(acceptor);
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
}
