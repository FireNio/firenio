package com.generallycloud.nio.container.jms.decode;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.container.jms.MQException;
import com.generallycloud.nio.container.jms.Message;

public interface MessageDecoder {

	public abstract Message decode(BaseReadFuture future) throws MQException;

}