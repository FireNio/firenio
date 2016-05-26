package com.gifisan.nio.plugin.jms.server;

import com.gifisan.nio.Attachment;

public class JMSSessionAttachment implements Attachment {

	private MQContext			context				= null;
	private TransactionSection	transactionSection		= null;
	private ConsumerPushHandle	consumerPushFailedHandle	= null;
	private Consumer			consumer				= null;

	public JMSSessionAttachment(MQContext context) {
		this.context = context;
		this.consumerPushFailedHandle = context.getConsumerPushFailedHandle();
	}

	public TransactionSection getTransactionSection() {
		return transactionSection;
	}

	public void setTransactionSection(TransactionSection transactionSection) {
		this.transactionSection = transactionSection;
	}

	public MQContext getContext() {
		return context;
	}

	public ConsumerPushHandle getConsumerPushFailedHandle() {
		return consumerPushFailedHandle;
	}

	protected Consumer getConsumer() {
		return consumer;
	}

	protected void setConsumer(Consumer consumer) {
		this.consumer = consumer;
	}
	
	

}
