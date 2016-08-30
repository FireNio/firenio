package com.generallycloud.nio.extend.plugin.jms.client;

import com.generallycloud.nio.extend.plugin.jms.Message;

public interface OnMessage {

	public abstract void onReceive(Message message);
}
