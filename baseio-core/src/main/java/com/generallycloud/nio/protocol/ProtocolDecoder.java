package com.generallycloud.nio.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.component.IOSession;

public interface ProtocolDecoder {

	// 可能会遭受一种攻击，比如最大可接收数据为100，客户端传输到99后暂停，
	// 这样多次以后可能会导致内存溢出
	public abstract IOReadFuture decode(IOSession session, ByteBuffer buffer) throws IOException;

}