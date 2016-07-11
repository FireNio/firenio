package com.gifisan.nio.extend.plugin.jms.decode;

import com.gifisan.nio.component.future.nio.NIOReadFuture;
import com.gifisan.nio.extend.plugin.jms.Message;

public class EmptyMessageDecoder implements MessageDecoder{

	public Message decode(NIOReadFuture future) {
		return null;
	}
}
