package com.generallycloud.nio.extend.plugin.jms.decode;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.extend.plugin.jms.MQException;
import com.generallycloud.nio.extend.plugin.jms.Message;

public interface MessageDecoder {

	public abstract Message decode(BaseReadFuture future) throws MQException;

}