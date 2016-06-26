package com.gifisan.nio.plugin.jms.server;

import com.gifisan.nio.Attachment;

public class MQSessionAttachment implements Attachment {

	private MQContext			context				;
	private TransactionSection	transactionSection		;
	private ConsumerPushHandle	consumerPushFailedHandle	;
	private Consumer			consumer				;

	public MQSessionAttachment(MQContext context) {
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
