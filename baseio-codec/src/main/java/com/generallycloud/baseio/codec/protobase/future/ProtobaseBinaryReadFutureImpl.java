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
package com.generallycloud.baseio.codec.protobase.future;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.component.Session;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;

/**
 *
 */
public class ProtobaseBinaryReadFutureImpl extends ProtobaseReadFutureImpl {

	private byte[]	binary;
	private int	binaryLength;
	private int	binaryLimit;

	public ProtobaseBinaryReadFutureImpl(SocketChannelContext context) {
		super(context);
	}
	
	public ProtobaseBinaryReadFutureImpl(SocketSession session, ByteBuf buf, boolean isBroadcast,
			int binaryLimit) throws IOException {
		super(session, buf, isBroadcast);
		this.binaryLimit = binaryLimit;
	}

	@Override
	protected int getAllLength(Session session, ByteBuf buf) {
		binaryLength = buf.getInt();
		return future_name_length + textLength + binaryLength;
	}

	@Override
	protected void reallocateBuf(ByteBuf buf, int all_length) {
		buf.reallocate(all_length, binaryLimit);
	}

	protected void gainBinary(ByteBuf buf, int offset) {
		buf.skipBytes(future_name_length + textLength);
		binary = buf.getBytes();
	}

	@Override
	public byte[] getBinary() {
		return binary;
	}

	@Override
	public int getBinaryLength() {
		return binaryLength;
	}

	@Override
	public boolean hasBinary() {
		return true;
	}

}
