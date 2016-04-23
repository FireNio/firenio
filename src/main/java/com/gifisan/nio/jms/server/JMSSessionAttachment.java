package com.gifisan.nio.jms.server;

import java.util.ArrayList;
import java.util.List;

import com.gifisan.nio.Attachment;

public class JMSSessionAttachment implements Attachment {

	private MQContext			context			= null;

	private TransactionSection	transactionSection	= null;

	private List<String>		queueNames		= new ArrayList<String>();

	private boolean			logined			= false;

	public JMSSessionAttachment(MQContext context) {
		this.context = context;
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

	public void addQueueName(String queueName) {
		this.queueNames.add(queueName);
	}

	public List<String> getQueueNames() {
		return queueNames;
	}

	public boolean isLogined() {
		return logined;
	}

	public void setLogined(boolean logined) {
		this.logined = logined;
	}

}
