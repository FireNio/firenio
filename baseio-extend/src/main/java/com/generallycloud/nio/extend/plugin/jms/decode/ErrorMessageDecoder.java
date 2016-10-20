package com.generallycloud.nio.extend.plugin.jms.decode;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.extend.plugin.jms.ErrorMessage;
import com.generallycloud.nio.extend.plugin.jms.Message;

public class ErrorMessageDecoder implements MessageDecoder{

	public Message decode(BaseReadFuture future) {
		return new ErrorMessage(future.getParameters().getIntegerParameter("code"));
	}
}
