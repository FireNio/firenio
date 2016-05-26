package com.gifisan.nio.plugin.rtp.client;

import java.io.Closeable;
import java.io.IOException;

import com.gifisan.nio.client.ClientContext;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.client.ClientUDPConnector;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.DatagramPacketAcceptor;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.protocol.DatagramPacket;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.MapMessage;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.plugin.jms.client.MessageConsumer;
import com.gifisan.nio.plugin.jms.client.MessageProducer;
import com.gifisan.nio.plugin.jms.client.OnMessage;
import com.gifisan.nio.plugin.jms.client.impl.DefaultMessageConsumer;
import com.gifisan.nio.plugin.jms.client.impl.DefaultMessageProducer;
import com.gifisan.nio.plugin.rtp.RTPException;
import com.gifisan.nio.plugin.rtp.server.RTPCreateRoomServlet;
import com.gifisan.nio.plugin.rtp.server.RTPJoinRoomServlet;


public class RTPClient implements Closeable {

	private ClientSession		session		= null;
	private String				roomID		= null;
	private int				roomIDNo		= -1;
	private MessageProducer		producer		= null;
	private MessageConsumer		consumer		= null;
	private ClientUDPConnector	connector		= null;
	private UDPReceiveHandle		receiveHandle	= null;
	private ClientContext		context		= null;

	
	//FIXME ..
	public RTPClient(ClientSession session, UDPReceiveHandle handle, String customerID) throws Exception {
		this.connector = new ClientUDPConnector(session);
		this.session = session;
		this.receiveHandle = handle;
		this.producer = new DefaultMessageProducer(session);
		this.consumer = new DefaultMessageConsumer(session);
		this.context = connector.getContext();
		
		
		try {
			this.consumer.receive(new OnMessage() {

				public void onReceive(Message message) {
					receiveHandle.onMessage(RTPClient.this, message);
				}
			});
		} catch (JMSException e) {
			throw new RTPException(e);
		}
	}

	public void sendDatagramPacket(DatagramPacket packet) throws RTPException {

		if (roomID == null) {
			throw new RTPException("none roomID,create room first");
		}

		connector.sendDatagramPacket(packet);
	}

	public boolean createRoom(String customerID) throws RTPException {

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

		this.roomIDNo = Integer.parseInt(roomID);

		this.inviteCustomer(customerID);

		return true;
	}

	public boolean joinRoom(String roomID) throws RTPException {

		ReadFuture future;

		try {
			future = session.request(RTPJoinRoomServlet.SERVICE_NAME, roomID);
		} catch (IOException e) {
			throw new RTPException(e.getMessage(), e);
		}

		return "T".equals(future.getText());
	}

	public void inviteCustomer(String customerID) throws RTPException {

		if (roomID == null) {
			throw new RTPException("none roomID,create room first");
		}

		MapMessage message = new MapMessage("msgID", customerID);
		
		message.put("cmd","invite");
		message.put("roomID",roomID);

		try {
			producer.offer(message);
			
		} catch (JMSException e) {
			throw new RTPException(e);
		}
	}
	
	public static final String MARK_INTERVAL = "MARK_INTERVAL";
	
	public static final String CURRENT_MARK = "CURRENT_MARK";
	
	public static final String GROUP_SIZE = "GROUP_SIZE";
	

	public void inviteReply(String customerID, int markinterval, long currentMark, int groupSize) throws RTPException {

		MapMessage message = new MapMessage("msgID", customerID);
		
		message.put("cmd","invite-reply");
		message.put(MARK_INTERVAL,markinterval);
		message.put(CURRENT_MARK,currentMark);
		message.put(GROUP_SIZE,groupSize);

		try {
			producer.offer(message);
		} catch (JMSException e) {
			throw new RTPException(e);
		}
	}
	
	public void onDatagramPacketReceived(DatagramPacketAcceptor acceptor){
		connector.onDatagramPacketReceived(acceptor);
	}

	public void close() throws IOException {
		CloseUtil.close(connector);
	}

	public int getRoomIDNo() {
		return roomIDNo;
	}

	public void setRoomID(String roomID) {
		this.roomID = roomID;
		this.roomIDNo = Integer.valueOf(roomID);
	}
	
	protected ClientContext getContext(){
		return context;
	}
}
