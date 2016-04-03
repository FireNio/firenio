package com.gifisan.nio.jms.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.gifisan.nio.jms.ByteMessage;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.Response;

public class Consumer {

	private String				queueName		= null;
	private JMSSessionAttachment	attachment	= null;
	private ConsumerQueue		consumerGroup	= null;
	private Response response = null;

	public Consumer(ConsumerQueue consumerGroup, JMSSessionAttachment attachment,Response response, String queueName) {
		this.consumerGroup = consumerGroup;
		this.queueName = queueName;
		this.attachment = attachment;
		this.response = response;
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

		Response response = this.response;
		
		response.write(content);
		
		if (msgType == 2) {
			
			response.flush();
			
		}else{
			ByteMessage byteMessage = (ByteMessage) message;

			byte [] bytes = byteMessage.getContent();
			
			response.setInputStream(new ByteArrayInputStream(bytes));
			
			response.flush();
		}
	}
}
