package com.gifisan.nio.component.protocol.fixedlength;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.IOReadFuture;
import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.fixedlength.future.FixedLengthReadFutureImpl;

/**
 * <pre>
 * [0       ~              3]
 *  0 ~ 3为一个Integer，表示后续报文长度
 *  如果长度为-1，则该future的心跳请求
 * </pre>
 */
public class FixedLengthProtocolDecoder implements ProtocolDecoder {
	
	public static final int	PROTOCOL_HADER				= 4;

	public IOReadFuture decode(TCPEndPoint endPoint) throws IOException {

		ByteBuffer header = ByteBuffer.allocate(PROTOCOL_HADER);

		int length = endPoint.read(header);

		if (length < 1) {
			if (length == -1) {
				CloseUtil.close(endPoint);
			}
			return null;
		}

		return new FixedLengthReadFutureImpl(endPoint.getSession(),header);
	}
}
