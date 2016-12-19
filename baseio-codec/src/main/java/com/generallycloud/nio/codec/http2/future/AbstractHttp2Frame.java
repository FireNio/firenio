package com.generallycloud.nio.codec.http2.future;

import com.generallycloud.nio.codec.http2.Http2SocketSession;
import com.generallycloud.nio.protocol.AbstractChannelReadFuture;

public abstract class AbstractHttp2Frame extends AbstractChannelReadFuture implements Http2Frame {

	protected byte flags;
	
	protected AbstractHttp2Frame(Http2SocketSession session) {
		super(session.getContext());
		
		Http2FrameHeader header = session.getLastReadFrameHeader();
		
		this.flags = header.getFlags();
		this.streamIdentifier = header.getStreamIdentifier();
		
	}

	protected int	streamIdentifier;

	@Override
	public int getStreamIdentifier() {
		return streamIdentifier;
	}

	@Override
	public void setStreamIdentifier(int streamIdentifier) {
		this.streamIdentifier = streamIdentifier;
	}

	@Override
	public byte getFlags() {
		return flags;
	}
}
