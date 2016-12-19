package com.generallycloud.nio.container.jms.decode;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.container.jms.Message;

public class EmptyMessageDecoder implements MessageDecoder{

	@Override
	public Message decode(ProtobaseReadFuture future) {
		return null;
	}
}
