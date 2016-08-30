package com.gifisan.nio.component.protocol.http11;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.IOReadFuture;
import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.http11.future.WebSocketReadFutureImpl;

//FIXME 心跳貌似由服务端发起
/**
 * <pre>
 * 
 *    0               1               2               3
      0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7
     +-+-+-+-+-------+-+-------------+-------------------------------+
     |F|R|R|R| opcode|M| Payload len |    Extended payload length    |
     |I|S|S|S|  (4)  |A|     (7)     |             (16/64)           |
     |N|V|V|V|       |S|             |   (if payload len==126/127)   |
     | |1|2|3|       |K|             |                               |
     +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - +
     |     Extended payload length continued, if payload len == 127  |
     + - - - - - - - - - - - - - - - +-------------------------------+
     |                               |Masking-key, if MASK set to 1  |
     +-------------------------------+-------------------------------+
     | Masking-key (continued)       |          Payload Data         |
     +-------------------------------- - - - - - - - - - - - - - - - +
     :                     Payload Data continued ...                :
     + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - +
     |                     Payload Data continued ...                |
     +---------------------------------------------------------------+
 * 
 * 
 * </pre>
 *
 */
public class WebSocketProtocolDecoder implements ProtocolDecoder {

	public static final int	TYPE_TEXT		= 1;
	public static final int	TYPE_BINARY		= 2;
	public static final int	TYPE_CLOSE		= 8;
	public static final int	TYPE_PING		= 9;
	public static final int	TYPE_PONG		= 10;

	public IOReadFuture decode(TCPEndPoint endPoint) throws IOException {

		ByteBuffer buffer = ByteBuffer.allocate(2);

		int length = endPoint.read(buffer);

		if (length < 1) {
			// FIXME 处理连接异常导致的关闭
			if (length == -1) {
				CloseUtil.close(endPoint);
			}
			return null;
		}

		return new WebSocketReadFutureImpl(endPoint.getSession(), buffer);
	}

}
