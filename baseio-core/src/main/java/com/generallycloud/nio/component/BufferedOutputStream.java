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
package com.generallycloud.nio.component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

//FIXME 用到这里的检查是否需要实例化
public class BufferedOutputStream extends OutputStream implements HeapOutputStream{

	private byte		cache[]	;
	private int		count	;

	public BufferedOutputStream() {
		this(128);
	}

	public BufferedOutputStream(byte[] buffer) {
		this.cache = buffer;
		this.count = buffer.length;
	}

	public BufferedOutputStream(int size) {
		if (size < 0) {
			throw new IllegalArgumentException("Negative initial size: " + size);
		}
		cache = new byte[size];
	}

	public void reset() {
		count = 0;
	}

	@Override
	public int size() {
		return count;
	}
	
	@Override
	public byte [] array(){
		return cache;
	}

	@Override
	public String toString() {
		if (count == 0) {
			return null;
		}
		return new String(cache, 0, count);
	}

	public String toString(Charset charset) {
		return new String(cache, 0, count, charset);
	}

	@Override
	public void write(int b) {
		int newcount = count + 1;
		if (newcount > cache.length) {
			cache = Arrays.copyOf(cache, cache.length << 1);
		}
		cache[count] = (byte) b;
		count = newcount;
	}

	@Override
	public void write(byte bytes[], int offset, int length) {
		int newcount = count + length;
		if (newcount > cache.length) {
			cache = Arrays.copyOf(cache, Math.max(cache.length << 1, newcount));
		}
		System.arraycopy(bytes, offset, cache, count, length);
		count = newcount;
		
	}

	@Override
	public void write(byte[] bytes) {
		write(bytes, 0, bytes.length);
	}

	public void write2OutputStream(OutputStream out) throws IOException {
		out.write(cache, 0, count);
	}
}
