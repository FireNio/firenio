package com.generallycloud.nio.container.jms.client;

import com.generallycloud.nio.container.jms.Message;

public interface OnMessage {

	public abstract void onReceive(Message message);
}
