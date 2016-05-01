package com.gifisan.nio.jms.client;

import com.gifisan.nio.jms.Message;

public interface OnMessage {

	public abstract void onReceive(Message message);
}
