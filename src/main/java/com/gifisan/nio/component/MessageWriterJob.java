package com.gifisan.nio.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.server.Consumer;
import com.gifisan.nio.jms.server.MessageGroup;

public class MessageWriterJob implements Runnable{

	private static final Logger logger = LoggerFactory.getLogger(MessageWriterJob.class);
	
	private MessageGroup messageGroup = null;
	
	private Consumer consumer = null;
	
	private Message message = null;
	
	public MessageWriterJob(MessageGroup messageGroup, Consumer consumer,Message message) {
		this.messageGroup = messageGroup;
		this.consumer = consumer;
		this.message = message;
	}
	
	public void run() {
		try {
			consumer.push(message);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			//回炉
			messageGroup.offer(message);
		}
		
	}
	
}
