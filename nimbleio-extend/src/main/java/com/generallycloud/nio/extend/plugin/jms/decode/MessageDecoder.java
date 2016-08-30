package com.generallycloud.nio.extend.plugin.jms.decode;

import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;
import com.generallycloud.nio.extend.plugin.jms.MQException;
import com.generallycloud.nio.extend.plugin.jms.Message;

public interface MessageDecoder {

	public abstract Message decode(NIOReadFuture future) throws MQException;

}