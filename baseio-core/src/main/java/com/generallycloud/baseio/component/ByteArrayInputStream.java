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
package com.generallycloud.baseio.component;

import java.io.IOException;
import java.io.InputStream;

public class ByteArrayInputStream extends InputStream {

	private byte	buf[];

	private int	pos;

	private int	mark	;

	private int	count;

	public ByteArrayInputStream(byte buf[]) {
		this.buf = buf;
		this.pos = 0;
		this.count = buf.length;
	}

	@Override
	public int read() {
		return (pos < count) ? (buf[pos++] & 0xff) : -1;
	}

	@Override
	public int read(byte b[], int off, int len) {
		if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		}
		if (pos >= count) {
			return -1;
		}
		if (pos + len > count) {
			len = count - pos;
		}
		if (len <= 0) {
			return 0;
		}
		System.arraycopy(buf, pos, b, off, len);
		pos += len;
		return len;
	}

	@Override
	public long skip(long n) {
		if (pos + n > count) {
			n = count - pos;
		}
		if (n < 0) {
			return 0;
		}
		pos += n;
		return n;
	}

	@Override
	public int available() {
		return count - pos;
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	@Override
	public void mark(int readAheadLimit) {
		mark = pos;
	}

	@Override
	public void reset() {
		pos = mark;
	}

	@Override
	public void close() throws IOException {
	}
	
	public byte [] toByteArray(){
		return buf;
	}

}
