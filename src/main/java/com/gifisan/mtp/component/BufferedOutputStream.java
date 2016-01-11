package com.gifisan.mtp.component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.gifisan.mtp.server.OutputStream;

public class BufferedOutputStream implements OutputStream {

	protected byte	cache[]	= null;
	protected int		count	= 0;

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

	public byte toByteArray()[] {
		return Arrays.copyOf(cache, count);
	}

	public String toString() {
		return new String(cache, 0, count);
	}

	public String toString(String charset) throws UnsupportedEncodingException {
		return new String(cache, 0, count, charset);
	}

	public void write(byte b) {

		int newcount = count + 1;
		if (newcount > cache.length) {
			cache = Arrays.copyOf(cache, cache.length << 1);
		}
		cache[count] = b;
		count = newcount;
	}

	public void write(byte bytes[], int off, int len) {
		// if ( (off < 0)
		// || (off > bytes.length)
		// || (len < 0)
		// || ((off + len) > bytes.length) || ((off + len) < 0)) {
		// throw new IndexOutOfBoundsException();
		// } else if (len == 0) {
		// return;
		// }
		int newcount = count + len;
		if (newcount > cache.length) {
			cache = Arrays.copyOf(cache, Math.max(cache.length << 1, newcount));
		}
		System.arraycopy(bytes, off, cache, count, len);
		count = newcount;
	}

	public void write(byte[] bytes) {
		this.write(bytes, 0, bytes.length);
	}

	public void writeTo(OutputStream out) throws IOException {
		out.write(cache, 0, count);
	}

}
