package com.generallycloud.nio.codec.http2;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.http2.future.Http2HeadersFrameImpl;
import com.generallycloud.nio.codec.http2.future.Http2PrefaceReadFuture;
import com.generallycloud.nio.codec.http2.future.Http2FrameHeaderImpl;
import com.generallycloud.nio.codec.http2.future.Http2SettingsFrameImpl;
import com.generallycloud.nio.codec.http2.future.Http2WindowUpdateFrameImpl;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;

/**
 * <pre>
 * +-----------------------------------------------+
 * |                 Length (24)                   |
 * +---------------+---------------+---------------+
 * |   Type (8)    |   Flags (8)   |
 * +-+-------------+---------------+-------------------------------+
 * |R|                 Stream Identifier (31)                      |
 * +=+=============================================================+
 * |                   Frame Payload (0...)                      ...
 * +---------------------------------------------------------------+
 * </pre>
 * <dl>
 * <dt>Length:</dt>
 * <dd>
 * <p>
 * The length of the frame payload expressed as an unsigned 24-bit integer.
 * Values greater than 2<sup>14</sup> (16,384) MUST NOT be sent unless the
 * receiver has set a larger value for <a href="#SETTINGS_MAX_FRAME_SIZE"
 * class="smpl">SETTINGS_MAX_FRAME_SIZE</a>.
 * </p>
 * <p>
 * The 9 octets of the frame header are not included in this value.
 * </p>
 * </dd>
 * <dt>Type:</dt>
 * <dd>
 * <p>
 * The 8-bit type of the frame. The frame type determines the format and
 * semantics of the frame. Implementations MUST ignore and discard any frame
 * that has a type that is unknown.
 * </p>
 * </dd>
 * <dt>Flags:</dt>
 * <dd>
 * <p>
 * An 8-bit field reserved for boolean flags specific to the frame type.
 * </p>
 * <p>
 * Flags are assigned semantics specific to the indicated frame type. Flags that
 * have no defined semantics for a particular frame type MUST be ignored and
 * MUST be left unset (0x0) when sending.
 * </p>
 * </dd>
 * <dt>R:</dt>
 * <dd>
 * <p>
 * A reserved 1-bit field. The semantics of this bit are undefined, and the bit
 * MUST remain unset (0x0) when sending and MUST be ignored when receiving.
 * </p>
 * </dd>
 * <dt>Stream Identifier:</dt>
 * <dd>
 * <p>
 * A stream identifier (see <a href="#StreamIdentifiers"
 * title="Stream Identifiers">Section&nbsp;5.1.1</a>) expressed as an unsigned
 * 31-bit integer. The value 0x0 is reserved for frames that are associated with
 * the connection as a whole as opposed to an individual stream.
 * </p>
 * </dd>
 * </dl>
 * 
 */
//http://httpwg.org/specs/rfc7540.html
public class Http2ProtocolDecoder implements ProtocolDecoder {

	public static final int	PROTOCOL_PREFACE_HEADER	= 24;

	public static final int	PROTOCOL_HEADER		= 9;

	public static final int	PROTOCOL_PING			= -1;

	public static final int	PROTOCOL_PONG			= -2;

	public IOReadFuture decode(SocketSession session, ByteBuffer buffer) throws IOException {

		Http2SocketSession http2UnsafeSession = (Http2SocketSession) session;

		BaseContext context = session.getContext();

		switch (http2UnsafeSession.getFrameWillBeRead()) {
		case FRAME_TYPE_CONTINUATION:

			break;
		case FRAME_TYPE_DATA:

			break;
		case FRAME_TYPE_FRAME_HEADER:
			return new Http2FrameHeaderImpl(session, allocate(context, PROTOCOL_HEADER));
		case FRAME_TYPE_GOAWAY:

			break;
		case FRAME_TYPE_HEADERS:
			return new Http2HeadersFrameImpl(http2UnsafeSession, allocate(context, http2UnsafeSession));
		case FRAME_TYPE_PING:

			break;
		case FRAME_TYPE_PREFACE:
			return new Http2PrefaceReadFuture(context, allocate(context, PROTOCOL_PREFACE_HEADER));
		case FRAME_TYPE_PRIORITY:

			break;
		case FRAME_TYPE_PUSH_PROMISE:

			break;
		case FRAME_TYPE_RST_STREAM:

			break;
		case FRAME_TYPE_SETTINGS:
			return new Http2SettingsFrameImpl(http2UnsafeSession, allocate(context, http2UnsafeSession));
		case FRAME_TYPE_WINDOW_UPDATE:
			return new Http2WindowUpdateFrameImpl(http2UnsafeSession, allocate(context, http2UnsafeSession));
		default:
			break;
		}
		return null;
	}

	private ByteBuf allocate(BaseContext context, int capacity) {
		return context.getHeapByteBufferPool().allocate(capacity);
	}
	
	private ByteBuf allocate(BaseContext context,Http2SocketSession session) {
		return context.getHeapByteBufferPool().allocate(session.getLastReadFrameHeader().getLength());
	}
}
