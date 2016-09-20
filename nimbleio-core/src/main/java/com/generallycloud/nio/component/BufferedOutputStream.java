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

	public int size() {
		return count;
	}

	public byte [] toByteArray() {
		return count > 0 ? Arrays.copyOf(cache, count) : null;
	}
	
	public byte [] array(){
		return cache;
	}

	public String toString() {
		if (count == 0) {
			return null;
		}
		return new String(cache, 0, count);
	}

	public String toString(Charset charset) {
		return new String(cache, 0, count, charset);
	}

	public void write(int b) {
		int newcount = count + 1;
		if (newcount > cache.length) {
			cache = Arrays.copyOf(cache, cache.length << 1);
		}
		cache[count] = (byte) b;
		count = newcount;
	}

	public void write(byte bytes[], int offset, int length) {
		int newcount = count + length;
		if (newcount > cache.length) {
			cache = Arrays.copyOf(cache, Math.max(cache.length << 1, newcount));
		}
		System.arraycopy(bytes, offset, cache, count, length);
		count = newcount;
		
	}

	public void write(byte[] bytes) {
		write(bytes, 0, bytes.length);
	}

	public void write2OutputStream(OutputStream out) throws IOException {
		out.write(cache, 0, count);
	}
}
