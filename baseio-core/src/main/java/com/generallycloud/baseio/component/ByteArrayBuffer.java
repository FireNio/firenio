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

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

//FIXME 用到这里的检查是否需要实例化
public class ByteArrayBuffer extends OutputStream {

	private byte	buffer[];
	private int	count;

	public ByteArrayBuffer() {
		this(128);
	}

	public ByteArrayBuffer(byte[] buffer) {
		this(buffer, buffer.length);
	}
	
	public ByteArrayBuffer(byte[] buffer,int length) {
		this.buffer = buffer;
		this.count = length;
	}

	public ByteArrayBuffer(int size) {
		if (size < 0) {
			throw new IllegalArgumentException("Negative initial size: " + size);
		}
		buffer = new byte[size];
	}

	public void reset() {
		count = 0;
	}

	public int size() {
		return count;
	}

	/**
	 * return the array of this stream, maybe size() < array().length
	 * 
	 * @return
	 */
	public byte[] array() {
		return buffer;
	}

	@Override
	public String toString() {
		if (count == 0) {
			return null;
		}
		return new String(buffer, 0, count);
	}

	public String toString(Charset charset) {
		return new String(buffer, 0, count, charset);
	}

	@Override
	public void write(int b) {
		int newcount = count + 1;
		if (newcount > buffer.length) {
			buffer = Arrays.copyOf(buffer, buffer.length << 1);
		}
		buffer[count] = (byte) b;
		count = newcount;
	}

	@Override
	public void write(byte bytes[], int offset, int length) {
		ensureCapacity(count + length);
		System.arraycopy(bytes, offset, buffer, count, length);
	}

	private void ensureCapacity(int newcount) {
		if (newcount > buffer.length) {
			int newLength = buffer.length + buffer.length >> 1;
			buffer = Arrays.copyOf(buffer, Math.max(newLength << 1, newcount));
		}
		this.count = newcount;
	}

	@Override
	public void write(byte[] bytes) {
		write(bytes, 0, bytes.length);
	}

}
