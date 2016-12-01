package com.generallycloud.nio.container.jms.client.impl;

import com.generallycloud.nio.container.jms.MapByteMessage;
import com.generallycloud.nio.container.jms.MapMessage;

public abstract class OnMappedMessage {
	
	public void onReceive(MapMessage message) {}
	public void onReceive(MapByteMessage message){}

}
