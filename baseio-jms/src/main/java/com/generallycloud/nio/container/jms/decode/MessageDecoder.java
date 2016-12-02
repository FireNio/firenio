package com.generallycloud.nio.container.jms.decode;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.container.jms.MQException;
import com.generallycloud.nio.container.jms.Message;

public interface MessageDecoder {

	public abstract Message decode(ProtobaseReadFuture future) throws MQException;

}