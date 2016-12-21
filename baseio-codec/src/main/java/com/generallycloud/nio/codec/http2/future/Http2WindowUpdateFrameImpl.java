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
package com.generallycloud.nio.codec.http2.future;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.http2.Http2SocketSession;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.SocketSession;

public class Http2WindowUpdateFrameImpl extends AbstractHttp2Frame implements Http2WindowUpdateFrame {

	private ByteBuf	buf;

	private boolean	isComplete;

	private int		updateValue;

	public Http2WindowUpdateFrameImpl(Http2SocketSession session, ByteBuf buf) {
		super(session);
		this.buf = buf;
	}

	private void doComplete(Http2SocketSession session, ByteBuf buf) throws IOException {
		
		this.updateValue = MathUtil.int2int31(buf.getInt());

		session.setFrameWillBeRead(Http2FrameType.FRAME_TYPE_FRAME_HEADER);
	}

	@Override
	public boolean read(SocketSession session, ByteBuf buffer) throws IOException {

		if (!isComplete) {

			ByteBuf buf = this.buf;

			buf.read(buffer);

			if (buf.hasRemaining()) {
				return false;
			}
			
			isComplete = true;
			
			doComplete((Http2SocketSession) session, buf.flip());
		}

		return true;
	}

	@Override
	public void release() {
		ReleaseUtil.release(buf);
	}

	@Override
	public boolean isSilent() {
		return true;
	}

	@Override
	public Http2FrameType getHttp2FrameType() {
		return Http2FrameType.FRAME_TYPE_SETTINGS;
	}

	@Override
	public int getUpdateValue() {
		return updateValue;
	}

}
