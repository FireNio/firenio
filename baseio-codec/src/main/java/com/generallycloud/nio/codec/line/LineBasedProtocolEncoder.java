package com.generallycloud.nio.codec.line;

import java.io.IOException;

import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.IOWriteFuture;
import com.generallycloud.nio.protocol.ProtocolEncoderImpl;

public class LineBasedProtocolEncoder extends ProtocolEncoderImpl {
	
	private byte lineBase = '\n';

	public IOWriteFuture encode(BaseContext context, IOReadFuture future) throws IOException {
		
		future.write(lineBase);
		
		return super.encode(context, future);
	}
	
}
