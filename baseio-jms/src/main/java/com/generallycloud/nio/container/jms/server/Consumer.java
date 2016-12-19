package com.generallycloud.nio.container.jms.server;

import java.io.IOException;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFutureImpl;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.jms.BytedMessage;
import com.generallycloud.nio.container.jms.Message;

public class Consumer {

	private String				queueName;
	private MQSessionAttachment	attachment;
	private ConsumerQueue		consumerQueue;
	private SocketSession		session;
	private ProtobaseReadFuture		future;
	private Message			message;

	public Consumer(ConsumerQueue consumerQueue, MQSessionAttachment attachment, SocketSession session,
			ProtobaseReadFuture future, String queueName) {
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

		SocketSession session = this.session;

		ProtobaseReadFuture f = new ProtobaseReadFutureImpl(session.getContext(), future.getFutureID(), future.getFutureName());

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

	@Override
	public Consumer clone() {
		ProtobaseReadFuture f = new ProtobaseReadFutureImpl(session.getContext(), future.getFutureID(), future.getFutureName());
		return new Consumer(consumerQueue, attachment, session, f, queueName);
	}
}
