package com.gifisan.nio.extend.plugin.jms.decode;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.extend.plugin.jms.MQException;
import com.gifisan.nio.extend.plugin.jms.Message;

public interface MessageDecoder {

	public abstract Message decode(ReadFuture future) throws MQException;

}