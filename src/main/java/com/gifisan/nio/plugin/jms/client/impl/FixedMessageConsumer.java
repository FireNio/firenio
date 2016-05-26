package com.gifisan.nio.plugin.jms.client.impl;

import java.util.HashMap;
import java.util.Map;

import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.plugin.jms.ErrorMessage;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.MapByteMessage;
import com.gifisan.nio.plugin.jms.MapMessage;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.plugin.jms.NullMessage;
import com.gifisan.nio.plugin.jms.TextByteMessage;
import com.gifisan.nio.plugin.jms.TextMessage;
import com.gifisan.nio.plugin.jms.client.MessageConsumer;
import com.gifisan.nio.plugin.jms.client.OnMessage;

public class FixedMessageConsumer implements OnMessage, MessageConsumer {

	private Map<String, OnMappedMessage>	onMappedMessages	= new HashMap<String, OnMappedMessage>();

	private OnNullMessage				onNullMessage		= null;

	private OnErrorMessage				onErrorMessage		= null;

	private OnTextByteMessage			onTextByteMessage	= null;

	private OnTextMessage				onTextMessage		= null;

	private MessageConsumer				messageConsumer	= null;

	public FixedMessageConsumer(ClientSession session) {
		this.messageConsumer = new DefaultMessageConsumer(session);
	}

	public void onReceive(Message message) {

		int msgType = message.getMsgType();

		if (Message.TYPE_MAP == msgType) {

			MapMessage m = (MapMessage) message;

			String eventName = m.getParameter("eventName");

			OnMappedMessage onMessage = onMappedMessages.get(eventName);

			if (onMessage == null) {
				return;
			}

			onMessage.onReceive(m);

		} else if (Message.TYPE_MAP_BYTE == msgType) {

			MapByteMessage m = (MapByteMessage) message;

			String eventName = m.getParameter("eventName");

			OnMappedMessage onMessage = onMappedMessages.get(eventName);

			if (onMessage == null) {
				return;
			}

			onMessage.onReceive(m);

		} else if (Message.TYPE_TEXT == msgType) {

			if (onTextMessage != null) {
				onTextMessage.onReceive((TextMessage) message);
			}

		} else if (Message.TYPE_TEXT_BYTE == msgType) {

			if (onTextByteMessage != null) {
				onTextByteMessage.onReceive((TextByteMessage) message);
			}

		} else if (Message.TYPE_ERROR == msgType) {

			if (onErrorMessage != null) {
				onErrorMessage.onReceive((ErrorMessage) message);
			}

		} else if (Message.TYPE_NULL == msgType) {

			if (onNullMessage != null) {
				onNullMessage.onReceive((NullMessage) message);
			}

		}
	}

	public void listenTextMessage(OnTextMessage onTextMessage) {
		this.onTextMessage = onTextMessage;
	}

	public void listenTextByteMessage(OnTextByteMessage onTextByteMessage) {
		this.onTextByteMessage = onTextByteMessage;
	}

	public void listenErrorMessage(OnErrorMessage onErrorMessage) {
		this.onErrorMessage = onErrorMessage;
	}

	public void listenNullMessage(OnNullMessage onNullMessage) {
		this.onNullMessage = onNullMessage;
	}

	public void listen(String eventName, OnMappedMessage onMapByteMessage) {
		this.onMappedMessages.put(eventName, onMapByteMessage);
	}

	public boolean beginTransaction() throws JMSException {
		return messageConsumer.beginTransaction();
	}

	public boolean commit() throws JMSException {
		return messageConsumer.commit();
	}

	public boolean rollback() throws JMSException {
		return messageConsumer.rollback();
	}

	public void receive(OnMessage onMessage) throws JMSException {

		if (onMessage != null) {
			throw new JMSException("");
		}

		messageConsumer.receive(this);
	}

	public void subscribe(OnMessage onMessage) throws JMSException {

		if (onMessage != null) {
			throw new JMSException("");
		}

		messageConsumer.subscribe(this);
	}

	public interface OnTextMessage {
		public abstract void onReceive(TextMessage message);
	}

	public interface OnTextByteMessage {
		public abstract void onReceive(TextByteMessage message);
	}

	public interface OnErrorMessage {
		public abstract void onReceive(ErrorMessage message);
	}

	public interface OnNullMessage {
		public abstract void onReceive(NullMessage message);
	}
}
