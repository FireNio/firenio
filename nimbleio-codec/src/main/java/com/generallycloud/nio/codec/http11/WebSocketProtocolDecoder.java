package com.generallycloud.nio.codec.http11;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.http11.future.WebSocketReadFutureImpl;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoderAdapter;

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
public class WebSocketProtocolDecoder extends ProtocolDecoderAdapter {

	public static final int	TYPE_TEXT		= 1;
	public static final int	TYPE_BINARY		= 2;
	public static final int	TYPE_CLOSE		= 8;
	public static final int	TYPE_PING		= 9;
	public static final int	TYPE_PONG		= 10;


	protected ByteBuf allocate(NIOContext context) {
		return context.getHeapByteBufferPool().allocate(2);
	}

	protected IOReadFuture fetchFuture(Session session, ByteBuf buffer) throws IOException {
		return new WebSocketReadFutureImpl(session, buffer);
	}

}
