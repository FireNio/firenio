package com.generallycloud.nio.codec.linebased.future;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.linebased.LineBasedProtocolDecoder;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.AbstractChannelReadFuture;

public class LineBasedReadFutureImpl extends AbstractChannelReadFuture implements LineBasedReadFuture {

	private boolean			complete;

	private int				limit;

	private BufferedOutputStream	cache	= new BufferedOutputStream();

	public LineBasedReadFutureImpl(SocketChannelContext context,int limit) {
		super(context);
		this.limit = limit;
	}
	
	public LineBasedReadFutureImpl(SocketChannelContext context) {
		super(context);
	}

	private void doBodyComplete() {
		
		this.readText = cache.toString(context.getEncoding());		

		this.complete = true;
	}

	@Override
	public boolean read(SocketSession session, ByteBuf buffer) throws IOException {

		if (complete) {
			return true;
		}

		BufferedOutputStream cache = this.cache;

		for (; buffer.hasRemaining();) {

			byte b = buffer.getByte();

			if (b == LineBasedProtocolDecoder.LINE_BASE) {
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

	@Override
	public void release() {
	}

	@Override
	public BufferedOutputStream getLineOutputStream() {
		return cache;
	}

}
