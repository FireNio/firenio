package com.gifisan.nio.jms.decode;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.jms.ErrorMessage;
import com.gifisan.nio.jms.Message;

public class ErrorMessageDecoder implements MessageDecoder{

	public Message decode(ReadFuture future) {
		return new ErrorMessage(future.getParameters().getIntegerParameter("code"));
	}
}
