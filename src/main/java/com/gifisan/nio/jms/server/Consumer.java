package com.gifisan.nio.jms.server;

import com.gifisan.nio.common.ReadFutureFactory;
import com.gifisan.nio.component.ByteArrayInputStream;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.jms.ByteMessage;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.IOSession;

public class Consumer {

	private String				queueName		= null;
	private JMSSessionAttachment	attachment	= null;
	private ConsumerQueue		consumerQueue	= null;
	private IOSession			session		= null;
	private ServerReadFuture		future		= null;
	private Message 			message 		= null;

	public Consumer(ConsumerQueue consumerQueue, JMSSessionAttachment attachment, IOSession session,
			ServerReadFuture future, String queueName) {
		this.consumerQueue = consumerQueue;
		this.queueName = queueName;
		this.attachment = attachment;
		this.session = session;
		this.future = future;
	}

	public String getQueueName() {
		return queueName;
	}

	public ConsumerQueue getConsumerQueue() {
		return consumerQueue;
	}

	//FIXME push 失败时对message进行回收,并移除Consumer
	public void push(Message message) {
		
		this.message = message;

		TransactionSection section = attachment.getTransactionSection();

		if (section != null) {
			section.offerMessage(message);
		}

		int msgType = message.getMsgType();

		String content = message.toString();

		IOSession session = this.session;
		
		future.attach(this);
		
		future.write(content);

		if (msgType == 2) {
			
			future.setInputIOEvent(null, attachment.getConsumerPushFailedHandle());

			session.flush(future);

		} else {
			ByteMessage byteMessage = (ByteMessage) message;

			byte[] bytes = byteMessage.getByteArray();

			future.setInputIOEvent(new ByteArrayInputStream(bytes), attachment.getConsumerPushFailedHandle());

			session.flush(future);

		}
	}
	
	public void refresh(){
		
		this.future = ReadFutureFactory.create(future);
	}
	
	public Message getMessage() {
		return message;
	}

	public Consumer clone(){
		return new Consumer(consumerQueue, attachment, session, ReadFutureFactory.create(future), queueName);
	}
}
