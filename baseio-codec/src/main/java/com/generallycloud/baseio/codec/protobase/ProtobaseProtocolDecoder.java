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
package com.generallycloud.baseio.codec.protobase;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseBinaryReadFutureImpl;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseReadFutureImpl;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.protocol.ChannelReadFuture;
import com.generallycloud.baseio.protocol.ProtocolDecoder;

/**
 * <pre>
 * 
 *  B0：
 *  +---------------------------------------------------------------------+
 *  |                                                                     |
 *  +      -       -       -       -       -       -       -       -      +
 *  |      0       1       2       3       4       5       6       7      | 
 *  +      -       -       -       -       -       -       -       -      +
 *  |       message    |  has  |   is    |                                |
 *  |        type      | binary|broadcast|                                |
 *  +---------------------------------------------------------------------+
 *  
 *  B0:0-1	: 报文类型 [0=UNKONW,1=PACKET,2=BEAT.PING,3=BEAT.PONG]
 *  B0:2  	: 推送类型 [0=PUSH,1=BRODCAST]
 *  B0:3		: 是否带有二进制数据[1=true,0=false]
 *  B0:4-7	: 预留
 *  B1		: future   name  length
 *  B4	 	: future   id
 *  B2	 	：text           length
 *  B4	 	：binary         length //FIXME 是否应该设置为两字节？
 * 
 * </pre>
 */
public class ProtobaseProtocolDecoder implements ProtocolDecoder {

	public static final int	PROTOCOL_HEADER_WITH_BINARY	= 12;
	public static final int	PROTOCOL_HEADER_NO_BINARY		= 8;

	public static final byte	PROTOCOL_PACKET				= (byte) 0b01000000;
	public static final byte	PROTOCOL_PING					= (byte) 0b10000000;
	public static final byte	PROTOCOL_PONG					= (byte) 0b11000000;
	public static final byte	PROTOCOL_HAS_BINARY			= (byte) 0b00100000;
	public static final byte	PROTOCOL_IS_BROADCAST			= (byte) 0b00010000;

	protected int			limit;

	public ProtobaseProtocolDecoder(int limit) {
		this.limit = limit;
	}

	@Override
	public ChannelReadFuture decode(SocketSession session, ByteBuf buffer) throws IOException {

		byte byte0 = buffer.getByte();

		if (byte0 == PROTOCOL_PING) {
			return new ProtobaseReadFutureImpl(session.getContext()).setPING();
		} else if (byte0 == PROTOCOL_PONG) {
			return new ProtobaseReadFutureImpl(session.getContext()).setPONG();
		}

		ByteBufAllocator allocator = session.getByteBufAllocator();

		if ((byte0 & PROTOCOL_HAS_BINARY) > 0) {
			return newChannelReadFutureWithBinary(session, allocator, byte0);
		}

		return newChannelReadFutureNoBinary(session, allocator, byte0);
	}

	protected boolean isBroadcast(byte b) {
		return (b & PROTOCOL_IS_BROADCAST) > 0;
	}

	protected ChannelReadFuture newChannelReadFutureNoBinary(SocketSession session,
			ByteBufAllocator allocator, byte b) throws IOException {
		ByteBuf buf = allocator.allocate(PROTOCOL_HEADER_NO_BINARY - 1);
		return new ProtobaseReadFutureImpl(session, buf);
	}

	protected ChannelReadFuture newChannelReadFutureWithBinary(SocketSession session,
			ByteBufAllocator allocator, byte b) throws IOException {
		ByteBuf buf = allocator.allocate(PROTOCOL_HEADER_WITH_BINARY - 1);
		return new ProtobaseBinaryReadFutureImpl(session, buf, limit);
	}

}
