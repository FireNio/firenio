package com.generallycloud.nio.codec.line.future;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.protocol.AbstractIOReadFuture;

public class LineBasedReadFutureImpl extends AbstractIOReadFuture implements LineBasedReadFuture {

	private String				text;

	private boolean			complete;

	private int				limit;

	private BufferedOutputStream	cache	= new BufferedOutputStream();

	public LineBasedReadFutureImpl(BaseContext context) {
		super(context);
		this.limit = 1024 * 1024;
	}

	private void doBodyComplete() {
		complete = true;
	}

	public boolean read(IOSession session, ByteBuffer buffer) throws IOException {

		if (complete) {
			return true;
		}

		BufferedOutputStream cache = this.cache;

		for (; buffer.hasRemaining();) {

			byte b = buffer.get();

			if (b == LINE_BASE) {
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

	public String getText() {
		return getText(context.getEncoding());
	}

	public String getText(Charset encoding) {

		if (text == null) {
			text = cache.toString(encoding);
		}

		return text;
	}

	public BufferedOutputStream getOutputStream() {
		return cache;
	}

	public void release() {
	}

}
