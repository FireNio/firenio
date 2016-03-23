package com.gifisan.nio.jms.server;

import com.gifisan.nio.Attachment;

public class JMSSessionAttachment implements Attachment {

	private MQContext			context;

	private TransactionSection	transactionSection	= null;

	public TransactionSection getTransactionSection() {
		return transactionSection;
	}

	public void setTransactionSection(TransactionSection transactionSection) {
		this.transactionSection = transactionSection;
	}

	public MQContext getContext() {
		return context;
	}

	public void setContext(MQContext context) {
		this.context = context;
	}

}
