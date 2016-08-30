package com.generallycloud.nio.component.protocol.fixedlength;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.TCPEndPoint;
import com.generallycloud.nio.component.protocol.IOReadFuture;
import com.generallycloud.nio.component.protocol.ProtocolDecoder;
import com.generallycloud.nio.component.protocol.fixedlength.future.FixedLengthReadFutureImpl;

/**
 * <pre>
 *  B0 - B3：
 *  +-----------------+-----------------+-----------------+-----------------+
 *  |        B0                B1                B2               B3        |
 *  + - - - - - - - - + - - - - - - - - + - - - - - - - - + - - - - - - - - +
 *  | 0 1 2 3 4 5 6 7   0 1 2 3 4 5 6 7   0 1 2 3 4 5 6 7   0 1 2 3 4 5 6 7 |
 *  | - - - - - - - - + - - - - - - - - + - - - - - - - - + - - - - - - - - +
 *  | B|                                                                    |
 *  | E|                       Data-length(P1-P31)                          |
 *  | A|                                                                    |
 *  | T|                                                                    |
 *  +-----------------+-----------------+-----------------+-----------------+
 *  
 *  Beat:1表示心跳，0表示正常报文
 *  Data-length:表示一个Java的Integer，用来表示报文的长度
 *  注意: 无论是否是心跳报文，报文头长度固定为4个字节
 *  
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
