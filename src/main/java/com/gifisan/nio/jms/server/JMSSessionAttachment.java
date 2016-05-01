package com.gifisan.nio.jms.server;

import java.util.ArrayList;
import java.util.List;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.component.ActiveAuthority;

public class JMSSessionAttachment implements Attachment {

	private MQContext			context			= null;
	private TransactionSection	transactionSection	= null;
	private ActiveAuthority		authority			= null;
	private List<String>		queueNames		= new ArrayList<String>();

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

	public ActiveAuthority getAuthority() {
		return authority;
	}

	protected void setAuthority(ActiveAuthority authority) {
		this.authority = authority;
	}

}
