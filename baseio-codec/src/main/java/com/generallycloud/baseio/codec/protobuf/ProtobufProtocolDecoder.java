/*
 * Copyright 2015-2017 GenerallyCloud.com
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
package com.generallycloud.baseio.codec.protobuf;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.codec.protobase.ProtobaseProtocolDecoder;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseReadFutureImpl;
import com.generallycloud.baseio.codec.protobuf.future.ProtobufReadFutureImpl;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.protocol.ChannelReadFuture;

public class ProtobufProtocolDecoder extends ProtobaseProtocolDecoder {

	public ProtobufProtocolDecoder(int limit) {
		super(limit);
	}

	@Override
	public ChannelReadFuture decode(SocketSession session, ByteBuf buffer) throws IOException {

		byte byte0 = buffer.getByte();

		if (byte0 == PROTOCOL_PING) {
			return new ProtobaseReadFutureImpl(session.getContext()).setPING();
		} else if (byte0 == PROTOCOL_PONG) {
			return new ProtobaseReadFutureImpl(session.getContext()).setPONG();
		}

		ByteBuf buf;
		
		if ((byte0 & PROTOCOL_HAS_BINARY) == 0) {
			throw new IOException("need binary for protobuf");
		}
		
		boolean isBroadcast = (byte0 & PROTOCOL_IS_BROADCAST) > 0;
		
		buf = session.getByteBufAllocator().allocate(PROTOCOL_HEADER - 1);

		return new ProtobufReadFutureImpl(session, buf, isBroadcast,limit);
	}

}
