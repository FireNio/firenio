package com.gifisan.nio.extend.plugin.jms.decode;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.extend.plugin.jms.ErrorMessage;
import com.gifisan.nio.extend.plugin.jms.Message;

public class ErrorMessageDecoder implements MessageDecoder{

	public Message decode(ReadFuture future) {
		return new ErrorMessage(future.getParameters().getIntegerParameter("code"));
	}
}
