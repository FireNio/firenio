package com.generallycloud.nio.codec.line;

import java.io.IOException;

import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ProtocolEncoderImpl;

public class LineBasedProtocolEncoder extends ProtocolEncoderImpl {
	
	private byte lineBase = '\n';

	public ChannelWriteFuture encode(BaseContext context, ChannelReadFuture future) throws IOException {
		
		future.write(lineBase);
		
		return super.encode(context, future);
	}
	
}
