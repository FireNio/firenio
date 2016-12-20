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
package com.generallycloud.nio.codec.linebased.future;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.linebased.LineBasedProtocolDecoder;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.AbstractChannelReadFuture;

public class LineBasedReadFutureImpl extends AbstractChannelReadFuture implements LineBasedReadFuture {

	private boolean			complete;

	private int				limit;

	private BufferedOutputStream	cache	= new BufferedOutputStream();

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

		BufferedOutputStream cache = this.cache;

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
	public BufferedOutputStream getLineOutputStream() {
		return cache;
	}

}
