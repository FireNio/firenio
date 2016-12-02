package com.generallycloud.nio.container.jms.decode;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.container.jms.ErrorMessage;
import com.generallycloud.nio.container.jms.Message;

public class ErrorMessageDecoder implements MessageDecoder{

	public Message decode(ProtobaseReadFuture future) {
		return new ErrorMessage(future.getParameters().getIntegerParameter("code"));
	}
}
