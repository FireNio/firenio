package com.generallycloud.nio.extend.plugin.jms.server;

import java.io.IOException;

import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.codec.nio.future.NIOReadFutureImpl;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.plugin.jms.BytedMessage;
import com.generallycloud.nio.extend.plugin.jms.Message;

public class Consumer {

	private String				queueName		;
	private MQSessionAttachment	attachment	;
	private ConsumerQueue		consumerQueue	;
	private Session			session		;
	private NIOReadFuture			future		;
	private Message			message		;

	public Consumer(ConsumerQueue consumerQueue, MQSessionAttachment attachment, Session session, NIOReadFuture future,
			String queueName) {
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

	// FIXME push 失败时对message进行回收,并移除Consumer
	public void push(Message message) throws IOException {

		this.message = message;

		TransactionSection section = attachment.getTransactionSection();

		if (section != null) {
			section.offerMessage(message);
		}

		int msgType = message.getMsgType();

		String content = message.toString();

		Session session = this.session;

		NIOReadFuture f = new NIOReadFutureImpl(future.getFutureID(), future.getFutureName());

		f.attach(this);
		
		f.setIOEventHandle(this.future.getIOEventHandle());

		f.write(content);

		if (msgType == Message.TYPE_TEXT || msgType == Message.TYPE_MAP) {

			session.flush(f);

		} else if (msgType == Message.TYPE_TEXT_BYTE || msgType == Message.TYPE_MAP_BYTE) {

			BytedMessage byteMessage = (BytedMessage) message;

			byte[] bytes = byteMessage.getByteArray();

			f.writeBinary(bytes);

			session.flush(f);
		}
	}

	public Message getMessage() {
		return message;
	}

	public Consumer clone() {
		NIOReadFuture f = new NIOReadFutureImpl(future.getFutureID(), future.getFutureName());
		return new Consumer(consumerQueue, attachment, session, f, queueName);
	}
}
