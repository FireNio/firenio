/*
 * Copyright 2015 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.nio.codec.protobuf;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.protobase.ProtobaseProtocolDecoder;
import com.generallycloud.nio.codec.protobuf.future.ProtobufReadFutureImpl;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;

public class ProtobufProtocolDecoder implements ProtocolDecoder {

	private int limit;

	public ProtobufProtocolDecoder() {
		this(1024 * 8);
	}

	public ProtobufProtocolDecoder(int limit) {
		this.limit = limit;
	}

	@Override
	public ChannelReadFuture decode(SocketSession session, ByteBuf buffer) throws IOException {

		ByteBuf buf = session.getByteBufAllocator().allocate(ProtobaseProtocolDecoder.PROTOCOL_HEADER);

		buf.read(buffer);

		byte _type = buffer.getByte(0);

		int type = (_type & 0xff) >> 6;

		if (type == ProtobaseProtocolDecoder.PROTOCOL_PING) {
			return new ProtobufReadFutureImpl(session.getContext()).setPING();
		} else if (type == ProtobaseProtocolDecoder.PROTOCOL_PONG) {
			return new ProtobufReadFutureImpl(session.getContext()).setPONG();
		}

		return new ProtobufReadFutureImpl(session, buf, limit);
	}

}
