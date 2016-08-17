package com.gifisan.nio.component.protocol.http11;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.future.IOReadFuture;
import com.gifisan.nio.component.protocol.http11.future.WebSocketReadFutureImpl;

public class WebSocketProtocolDecoder implements ProtocolDecoder {

	public IOReadFuture decode(TCPEndPoint endPoint) throws IOException {

		ByteBuffer buffer = ByteBuffer.allocate(2);

		int length = endPoint.read(buffer);

		if (length < 1) {
			//FIXME 处理连接异常导致的关闭
			if (length == -1) {
				CloseUtil.close(endPoint);
			}
			return null;
		}

		return new WebSocketReadFutureImpl(endPoint.getSession(), buffer);
	}

}
