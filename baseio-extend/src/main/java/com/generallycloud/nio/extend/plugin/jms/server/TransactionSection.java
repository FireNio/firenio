package com.generallycloud.nio.extend.plugin.jms.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.generallycloud.nio.extend.plugin.jms.Message;
import com.generallycloud.nio.extend.plugin.jms.Transaction;

public class TransactionSection implements Transaction {

	private List<Message>	messages	= new ArrayList<Message>();
	private MQContext		context	;
	private AtomicBoolean	finish	= new AtomicBoolean(false);

	public TransactionSection(MQContext context) {
		this.context = context;
	}

	public boolean beginTransaction() {
		return false;
	}

	public boolean commit() {
		if (finish.compareAndSet(false, true)) {
			this.messages.clear();
			return true;
		}
		return false;
	}

	public boolean rollback(){
		if (finish.compareAndSet(false, true)) {
			MQContext context = this.context;
			for (Message message : messages) {
				context.offerMessage(message);
			}
			return true;
		}
		return false;
	}

	public void offerMessage(Message message) {
		if (finish.get()) {
			return;
		}
		this.messages.add(message);
	}

}
