package com.generallycloud.nio.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.component.IOSession;

public interface ProtocolDecoder {

	public abstract IOReadFuture decode(IOSession session, ByteBuffer buffer) throws IOException;

}