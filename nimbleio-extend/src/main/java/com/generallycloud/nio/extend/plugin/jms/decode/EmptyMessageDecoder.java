package com.generallycloud.nio.extend.plugin.jms.decode;

import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;
import com.generallycloud.nio.extend.plugin.jms.Message;

public class EmptyMessageDecoder implements MessageDecoder{

	public Message decode(NIOReadFuture future) {
		return null;
	}
}
