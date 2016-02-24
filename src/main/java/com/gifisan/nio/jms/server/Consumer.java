package com.gifisan.nio.jms.server;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;
import com.gifisan.nio.server.session.Session;

public class Consumer {

	private static final Logger	logger		= LoggerFactory.getLogger(Consumer.class);
	private ConsumerGroup		consumerGroup	= null;
	private String				queueName		= null;
	private Request			request		= null;
	private Response			response		= null;

	public Consumer(Request request, Response response, ConsumerGroup consumerGroup, String queueName) {
		this.request = request;
		this.response = response;
		this.consumerGroup = consumerGroup;
		this.queueName = queueName;
	}

	public ConsumerGroup getConsumerGroup() {
		return consumerGroup;
	}

	public String getQueueName() {
		return queueName;
	}

	private TransactionSection getTransactionSection(Session session) {

		return (TransactionSection) session.getAttribute("_MQ_TRANSACTION");
	}

	public void push(Message message) throws IOException {
		Request request = this.request;
		Response response = this.response;

		Session session = request.getSession();

		TransactionSection section = getTransactionSection(session);

		if (section != null) {
			section.offerMessage(message);
		}

		String content = message.toString();

		response.write(content);

		try {
			response.flush();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			session.disconnect();
		}
	}
}
