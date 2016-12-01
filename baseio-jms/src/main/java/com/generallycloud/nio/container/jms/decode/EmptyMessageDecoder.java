package com.generallycloud.nio.container.jms.decode;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.container.jms.Message;

public class EmptyMessageDecoder implements MessageDecoder{

	public Message decode(BaseReadFuture future) {
		return null;
	}
}
