package com.gifisan.nio.jms.server;

import java.io.IOException;

import com.gifisan.nio.component.ByteArrayInputStream;
import com.gifisan.nio.jms.ByteMessage;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.session.Session;

public class Consumer {

	private String				queueName		= null;
	private JMSSessionAttachment	attachment	= null;
	private ConsumerQueue		consumerGroup	= null;
	private Session			session		= null;

	public Consumer(ConsumerQueue consumerGroup, JMSSessionAttachment attachment, Session session, String queueName) {
		this.consumerGroup = consumerGroup;
		this.queueName = queueName;
		this.attachment = attachment;
		this.session = session;
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

		Session session = this.session;

		session.write(content);

		if (msgType == 2) {

			session.flush();

		} else {
			ByteMessage byteMessage = (ByteMessage) message;

			byte[] bytes = byteMessage.getByteArray();

			session.setInputStream(new ByteArrayInputStream(bytes));

			session.flush();
		}
	}
}
