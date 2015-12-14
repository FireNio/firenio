package com.gifisan.mtp.component;

import java.io.IOException;
import java.io.InputStream;

public class BufferedInputStream extends InputStream {

	protected byte buf[];

	protected int count;

	protected int mark;

	protected int pos;

	public BufferedInputStream(byte buf[]) {
		this.buf = buf;
		this.pos = 0;
		this.count = buf.length;
		this.mark = 0;
	}

	public BufferedInputStream(byte buf[], int offset, int length) {
		this.buf = buf;
		this.pos = offset;
		this.count = Math.min(offset + length, buf.length);
		this.mark = offset;
	}

	public int available() {
		return count - pos;
	}

	public void close() throws IOException {
	}

	public void mark(int readAheadLimit) {
		mark = pos;
	}

	public boolean markSupported() {
		return true;
	}

	public int read() {
		return (pos < count) ? (buf[pos++] & 0xff) : -1;
	}

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

	public void reset() {
		pos = mark;
	}

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

}
