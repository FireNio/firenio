package com.generallycloud.nio.codec.line.future;

import java.io.IOException;
import java.nio.charset.Charset;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.AbstractChannelReadFuture;

public class LineBasedReadFutureImpl extends AbstractChannelReadFuture implements LineBasedReadFuture {

	private String				text;

	private boolean			complete;

	private int				limit;

	private BufferedOutputStream	cache	= new BufferedOutputStream();

	public LineBasedReadFutureImpl(SocketChannelContext context) {
		super(context);
		this.limit = 1024 * 1024;
	}

	private void doBodyComplete() {
		complete = true;
	}

	public boolean read(SocketSession session, ByteBuf buffer) throws IOException {

		if (complete) {
			return true;
		}

		BufferedOutputStream cache = this.cache;

		for (; buffer.hasRemaining();) {

			byte b = buffer.getByte();

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

	public void release() {
	}

	public BufferedOutputStream getLineOutputStream() {
		return cache;
	}

}
