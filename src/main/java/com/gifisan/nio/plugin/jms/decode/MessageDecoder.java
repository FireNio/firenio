package com.gifisan.nio.plugin.jms.decode;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.Message;

public interface MessageDecoder {

	public abstract Message decode(ReadFuture future) throws JMSException;

}