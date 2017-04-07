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
package com.generallycloud.baseio.codec.linebased.future;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.codec.linebased.LineBasedProtocolDecoder;
import com.generallycloud.baseio.component.ByteArrayBuffer;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.protocol.AbstractChannelReadFuture;

public class LineBasedReadFutureImpl extends AbstractChannelReadFuture implements LineBasedReadFuture {

	private boolean			complete;

	private int				limit;

	private ByteArrayBuffer	cache	= new ByteArrayBuffer();

	public LineBasedReadFutureImpl(SocketChannelContext context,int limit) {
		super(context);
		this.limit = limit;
	}
	
	public LineBasedReadFutureImpl(SocketChannelContext context) {
		super(context);
	}

	private void doBodyComplete() {
		
		this.readText = cache.toString(context.getEncoding());		

		this.complete = true;
	}

	@Override
	public boolean read(SocketSession session, ByteBuf buffer) throws IOException {

		if (complete) {
			return true;
		}

		ByteArrayBuffer cache = this.cache;

		for (; buffer.hasRemaining();) {

			byte b = buffer.getByte();

			if (b == LineBasedProtocolDecoder.LINE_BASE) {
				doBodyComplete();
				return true;
			}

			cache.write(b);

			if (cache.size() > limit) {
				throw new IOException("max length " + limit);
			}
		}

		return false;
	}

	@Override
	public void release() {
	}

	@Override
	public ByteArrayBuffer getLineOutputStream() {
		return cache;
	}

}
