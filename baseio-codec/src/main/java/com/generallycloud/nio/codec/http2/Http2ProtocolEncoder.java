package com.generallycloud.nio.codec.http2;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.http2.future.Http2Frame;
import com.generallycloud.nio.codec.http2.future.Http2FrameType;
import com.generallycloud.nio.codec.http2.future.Http2SettingsFrame;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.IOWriteFuture;
import com.generallycloud.nio.protocol.IOWriteFutureImpl;
import com.generallycloud.nio.protocol.ProtocolEncoder;

public class Http2ProtocolEncoder implements ProtocolEncoder {

	public IOWriteFuture encode(BaseContext context, IOReadFuture future) throws IOException {
		
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

			Http2SettingsFrame f = (Http2SettingsFrame) frame;
			
			long [] settings = f.getSettings();
			
			payload = new byte[6 * settings.length];
			
			for (int i = 1; i < 7; i++) {
				
				int offset = i * 6;
				
				MathUtil.intTo2Byte(payload, i, offset);
				MathUtil.longTo4Byte(payload, settings[i], offset + 2);
				
			}
			
			break;
		case FRAME_TYPE_WINDOW_UPDATE:

			break;
		default:
			break;
		}
		
		int length = payload.length;
		
		ByteBuf buffer = context.getHeapByteBufferPool().allocate(length + Http2ProtocolDecoder.PROTOCOL_HEADER);
		
		int offset = buffer.offset();
		
		byte [] array = buffer.array();
		
		array[offset + 2] = (byte) ((length & 0xff));
		array[offset + 1] = (byte) ((length >> 8*1) & 0xff);
		array[offset + 0] = (byte) ((length >> 8*2) & 0xff);
		array[offset + 3] = frameType.getByteValue();
		
		MathUtil.int2Byte(array, 0, 5);
		
		buffer.position(9);
		
		buffer.put(payload);
		
		buffer.flip();
		
		return new IOWriteFutureImpl(future, buffer);
	}
}
