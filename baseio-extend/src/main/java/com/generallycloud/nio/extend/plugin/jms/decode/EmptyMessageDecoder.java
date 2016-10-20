package com.generallycloud.nio.extend.plugin.jms.decode;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.extend.plugin.jms.Message;

public class EmptyMessageDecoder implements MessageDecoder{

	public Message decode(BaseReadFuture future) {
		return null;
	}
}
