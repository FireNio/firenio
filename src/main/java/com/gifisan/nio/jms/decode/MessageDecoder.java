package com.gifisan.nio.jms.decode;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.jms.JMSException;
import com.gifisan.nio.jms.Message;

public interface MessageDecoder {

	public abstract Message decode(ReadFuture future) throws JMSException;

}