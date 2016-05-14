package com.gifisan.nio.plugin.jms.client;

import com.gifisan.nio.plugin.jms.Message;

public interface OnMessage {

	public abstract void onReceive(Message message);
}
