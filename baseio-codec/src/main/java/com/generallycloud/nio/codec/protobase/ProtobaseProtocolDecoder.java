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
package com.generallycloud.nio.codec.protobase;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFutureImpl;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;

/**
 * <pre>
 * 
 *  B0：
 *  +---------------------------------------------------------------------+
 *  |                      B0                                             |
 *  +      -       -       -       -       -       -       -       -      +
 *  |      0       1       2       3       4       5       6       7      | 
 *  +      -       -       -       -       -       -       -       -      +
 *  |       Message     | PUSH |  has  |                                 |
 *  |       T Y P E     | TYPE | binary|                                 |
 *  +---------------------------------------------------------------------+
 *  
 *  B0:0-1	: 报文类型 [0=UNKONW,1=PACKET,2=BEAT.PING,3=BEAT.PONG]
 *  B0:2  	: 推送类型 [0=PUSH,1=BRODCAST]
 *  B0:3		: 是否带有二进制数据1=true,0=false
 *  B0:4-7	: 预留
 *  B1		: future  name  length
 *  B2  - B5 	: future  id
 *  B6  - B9 	: session id
 *  B10 - B13 	: hash    code
 *  B14 - B15 	：text          length
 *  B16 - B19 	：binary        length //FIXME 是否应该设置为两字节？
 * 
 * </pre>
 */
public class ProtobaseProtocolDecoder implements ProtocolDecoder {

	public static final int	PROTOCOL_HEADER	= 20;

	public static final int	PROTOCOL_PACKET	= 1;
	public static final int	PROTOCOL_PING		= 2;
	public static final int	PROTOCOL_PONG		= 3;

	private int			limit;

	public ProtobaseProtocolDecoder(int limit) {
		this.limit = limit;
	}

	@Override
	public ChannelReadFuture decode(SocketSession session, ByteBuf buffer) throws IOException {

		ByteBuf buf = session.getByteBufAllocator().allocate(1);

		buf.read(buffer);

		byte byte0 = buffer.getByte(0);

		int type = (byte0 & 0xff) >> 6;

		if (type == PROTOCOL_PING) {
			return new ProtobaseReadFutureImpl(session.getContext()).setPING();
		} else if (type == PROTOCOL_PONG) {
			return new ProtobaseReadFutureImpl(session.getContext()).setPONG();
		}

		if ((byte0 & 0x10) > 0) {
			buf.reallocate(PROTOCOL_HEADER).skipBytes(1);
		}else{
			buf.reallocate(PROTOCOL_HEADER - 4).skipBytes(1);
		}
		
		return new ProtobaseReadFutureImpl(session, buf, limit);
	}

}
