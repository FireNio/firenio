package com.gifisan.nio.jms.server;

import java.io.IOException;

import com.gifisan.nio.component.ByteArrayInputStream;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.jms.ByteMessage;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.IOSession;

public class Consumer {

	private String				queueName		= null;
	private JMSSessionAttachment	attachment	= null;
	private ConsumerQueue		consumerGroup	= null;
	private IOSession			session		= null;
	private ServerReadFuture		future		= null;

	public Consumer(ConsumerQueue consumerGroup, JMSSessionAttachment attachment, IOSession session,
			ServerReadFuture future, String queueName) {
		this.consumerGroup = consumerGroup;
		this.queueName = queueName;
		this.attachment = attachment;
		this.session = session;
		this.future = future;
	}

	public String getQueueName() {
		return queueName;
	}

	public ConsumerQueue getConsumerGroup() {
		return consumerGroup;
	}

	public void push(Message message) throws IOException {

		TransactionSection section = attachment.getTransactionSection();

		if (section != null) {
			section.offerMessage(message);
		}

		int msgType = message.getMsgType();

		String content = message.toString();

		IOSession session = this.session;

		future.write(content);

		if (msgType == 2) {

			session.flush(future);

		} else {
			ByteMessage byteMessage = (ByteMessage) message;

			byte[] bytes = byteMessage.getByteArray();

			future.setInputIOEvent(new ByteArrayInputStream(bytes), null);

			session.flush(future);

		}
	}
}
