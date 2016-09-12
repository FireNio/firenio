package com.generallycloud.nio.component.protocol;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.TCPEndPoint;

public abstract class ProtocolDecoderAdapter implements ProtocolDecoder {

	public IOReadFuture decode(TCPEndPoint endPoint) throws IOException {
		
		ByteBuf buffer = allocate(endPoint.getContext());

		int length = -1;
		
		try {
			length = buffer.read(endPoint);
		} finally {
			if (length == -1) {
				ReleaseUtil.release(buffer);
				return null;
			}
		}

		return fetchFuture(endPoint.getSession(), buffer);
	}
	
	protected abstract ByteBuf allocate(NIOContext context);

	protected abstract IOReadFuture fetchFuture(Session session, ByteBuf buffer) throws IOException ;

}