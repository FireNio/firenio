/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.baseio.container.jms.server;

import java.io.IOException;

import com.generallycloud.baseio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseReadFutureImpl;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.jms.BytedMessage;
import com.generallycloud.baseio.container.jms.Message;

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

		ProtobaseReadFuture f = new ProtobaseReadFutureImpl(session.getContext(), future.getFutureId(), future.getFutureName());

		f.attach(this);

		f.setIoEventHandle(this.future.getIoEventHandle());

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
		ProtobaseReadFuture f = new ProtobaseReadFutureImpl(session.getContext(), future.getFutureId(), future.getFutureName());
		return new Consumer(consumerQueue, attachment, session, f, queueName);
	}
}
