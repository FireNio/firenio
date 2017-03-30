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
package com.generallycloud.baseio.protocol;

import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.SocketChannelContext;

public abstract class AbstractReadFuture extends AbstractFuture implements ReadFuture {

	protected boolean				flushed;
	protected String				readText;
	protected SocketChannelContext	context;
	protected IoEventHandle			ioEventHandle;
	protected StringBuilder			writeTextBuffer = new StringBuilder();

	protected AbstractReadFuture(SocketChannelContext context) {
		this.context = context;
	}

	@Override
	public boolean flushed() {
		return flushed;
	}

	@Override
	public SocketChannelContext getContext() {
		return context;
	}

	@Override
	public IoEventHandle getIoEventHandle() {
		if (ioEventHandle == null) {
			this.ioEventHandle = context.getIoEventHandleAdaptor();
		}
		return ioEventHandle;
	}

	@Override
	public String getReadText() {
		return readText;
	}

	@Override
	public String getWriteText() {
		return writeTextBuffer.toString();
	}

	@Override
	public StringBuilder getWriteTextBuffer() {
		return writeTextBuffer;
	}

	@Override
	public void setIoEventHandle(IoEventHandle ioEventHandle) {
		this.ioEventHandle = ioEventHandle;
	}

	@Override
	public void write(boolean b) {
		writeTextBuffer.append(b);
	}

	@Override
	public void write(char c) {
		writeTextBuffer.append(c);
	}

	@Override
	public void write(double d) {
		writeTextBuffer.append(d);
	}

	@Override
	public void write(int i) {
		writeTextBuffer.append(i);
	}

	@Override
	public void write(long l) {
		writeTextBuffer.append(l);
	}

	@Override
	public void write(String text) {
		if (StringUtil.isNullOrBlank(text)) {
			return;
		}
		writeTextBuffer.append(text);
	}

	@Override
	public String toString() {
		return getReadText();
	}
}
