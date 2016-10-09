package com.generallycloud.nio.extend.plugin.jms.decode;

import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.extend.plugin.jms.ErrorMessage;
import com.generallycloud.nio.extend.plugin.jms.Message;

public class ErrorMessageDecoder implements MessageDecoder{

	public Message decode(NIOReadFuture future) {
		return new ErrorMessage(future.getParameters().getIntegerParameter("code"));
	}
}
