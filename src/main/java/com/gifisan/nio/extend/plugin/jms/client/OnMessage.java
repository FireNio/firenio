package com.gifisan.nio.extend.plugin.jms.client;

import com.gifisan.nio.extend.plugin.jms.Message;

public interface OnMessage {

	public abstract void onReceive(Message message);
}
