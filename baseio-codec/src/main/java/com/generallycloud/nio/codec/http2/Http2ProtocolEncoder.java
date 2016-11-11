package com.generallycloud.nio.codec.http2;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.http2.future.Http2Frame;
import com.generallycloud.nio.codec.http2.future.Http2FrameType;
import com.generallycloud.nio.codec.http2.future.Http2HeadersFrame;
import com.generallycloud.nio.codec.http2.future.Http2SettingsFrame;
import com.generallycloud.nio.codec.http2.hpack.DefaultHttp2HeadersEncoder;
import com.generallycloud.nio.codec.http2.hpack.Http2HeadersEncoder;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ChannelWriteFutureImpl;
import com.generallycloud.nio.protocol.ProtocolEncoder;

public class Http2ProtocolEncoder implements ProtocolEncoder {
	
	private Http2HeadersEncoder http2HeadersEncoder = new DefaultHttp2HeadersEncoder();

	public ChannelWriteFuture encode(BaseContext context, ChannelReadFuture future) throws IOException {
		
		Http2Frame frame = (Http2Frame) future;
		
		Http2FrameType frameType = frame.getHttp2FrameType();
		
		byte [] payload = null;
		
		switch (frameType) {
		case FRAME_TYPE_CONTINUATION:

			break;
		case FRAME_TYPE_DATA:

			break;
		case FRAME_TYPE_FRAME_HEADER:
			
			break;
		case FRAME_TYPE_GOAWAY:

			break;
		case FRAME_TYPE_HEADERS:

			Http2HeadersFrame hf = (Http2HeadersFrame) frame;
			
			
//			http2HeadersEncoder.encodeHeaders(headers, buffer);
			
			
			
			break;
		case FRAME_TYPE_PING:

			break;
		case FRAME_TYPE_PREFACE:
			break;
		case FRAME_TYPE_PRIORITY:

			break;
		case FRAME_TYPE_PUSH_PROMISE:

			break;
		case FRAME_TYPE_RST_STREAM:

			break;
		case FRAME_TYPE_SETTINGS:

			Http2SettingsFrame sf = (Http2SettingsFrame) frame;
			
			long [] settings = sf.getSettings();
			
			payload = new byte[6 * settings.length];
			
			for (int i = 1; i < 7; i++) {
				
				int offset = i * 6;
				
				MathUtil.unsignedShort2Byte(payload, i, offset);
				MathUtil.unsignedInt2Byte(payload, settings[i], offset + 2);
			}
			
			break;
		case FRAME_TYPE_WINDOW_UPDATE:

			break;
		default:
			break;
		}
		
		int length = payload.length;
		
		ByteBuf buf = context.getByteBufAllocator().allocate(length + Http2ProtocolDecoder.PROTOCOL_HEADER);
		
		byte b2 = (byte) ((length & 0xff));
		byte b1 = (byte) ((length >> 8*1) & 0xff);
		byte b0 = (byte) ((length >> 8*2) & 0xff);
		byte b3 = frameType.getByteValue();
		
		buf.putByte(b0);
		buf.putByte(b1);
		buf.putByte(b2);
		buf.putByte(b3);
		
		buf.putInt(frame.getStreamIdentifier());
		
		buf.put(payload);
		
		buf.flip();
		
		return new ChannelWriteFutureImpl(future, buf);
	}
}
