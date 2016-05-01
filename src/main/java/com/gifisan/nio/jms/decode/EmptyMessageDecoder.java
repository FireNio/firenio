package com.gifisan.nio.jms.decode;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.jms.Message;

public class EmptyMessageDecoder implements MessageDecoder{

	public Message decode(ReadFuture future) {
		return null;
	}
}
