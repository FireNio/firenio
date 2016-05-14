package com.gifisan.nio.plugin.jms.decode;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.plugin.jms.Message;

public class EmptyMessageDecoder implements MessageDecoder{

	public Message decode(ReadFuture future) {
		return null;
	}
}
