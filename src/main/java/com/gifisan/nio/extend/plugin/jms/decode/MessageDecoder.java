package com.gifisan.nio.extend.plugin.jms.decode;

import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.extend.plugin.jms.MQException;
import com.gifisan.nio.extend.plugin.jms.Message;

public interface MessageDecoder {

	public abstract Message decode(NIOReadFuture future) throws MQException;

}