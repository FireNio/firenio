package com.generallycloud.nio.extend.plugin.jms.client.impl;

import java.util.HashMap;
import java.util.Map;

import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.plugin.jms.ErrorMessage;
import com.generallycloud.nio.extend.plugin.jms.MQException;
import com.generallycloud.nio.extend.plugin.jms.MapByteMessage;
import com.generallycloud.nio.extend.plugin.jms.MapMessage;
import com.generallycloud.nio.extend.plugin.jms.Message;
import com.generallycloud.nio.extend.plugin.jms.NullMessage;
import com.generallycloud.nio.extend.plugin.jms.TextByteMessage;
import com.generallycloud.nio.extend.plugin.jms.TextMessage;
import com.generallycloud.nio.extend.plugin.jms.client.MessageConsumer;
import com.generallycloud.nio.extend.plugin.jms.client.OnMessage;

public class FixedMessageConsumer implements OnMessage, MessageConsumer {

	private Map<String, OnMappedMessage>	onMappedMessages	= new HashMap<String, OnMappedMessage>();

	private OnNullMessage				onNullMessage		;

	private OnErrorMessage				onErrorMessage		;

	private OnTextByteMessage			onTextByteMessage	;

	private OnTextMessage				onTextMessage		;

	private MessageConsumer				messageConsumer	;

	public FixedMessageConsumer(FixedSession session) {
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

	public boolean beginTransaction() throws MQException {
		return messageConsumer.beginTransaction();
	}

	public boolean commit() throws MQException {
		return messageConsumer.commit();
	}

	public boolean rollback() throws MQException {
		return messageConsumer.rollback();
	}

	public void receive(OnMessage onMessage) throws MQException {

		if (onMessage != null) {
			throw new MQException("");
		}

		messageConsumer.receive(this);
	}

	public void subscribe(OnMessage onMessage) throws MQException {

		if (onMessage != null) {
			throw new MQException("");
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
