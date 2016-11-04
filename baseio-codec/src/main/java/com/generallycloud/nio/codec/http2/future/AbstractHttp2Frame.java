package com.generallycloud.nio.codec.http2.future;

import com.generallycloud.nio.codec.http2.Http2SocketSession;
import com.generallycloud.nio.protocol.AbstractIOReadFuture;

public abstract class AbstractHttp2Frame extends AbstractIOReadFuture implements Http2Frame {

	protected byte flags;
	
	protected AbstractHttp2Frame(Http2SocketSession session) {
		super(session.getContext());
		
		Http2FrameHeader header = session.getLastReadFrameHeader();
		
		this.flags = header.getFlags();
		this.streamIdentifier = header.getStreamIdentifier();
		
	}

	protected int	streamIdentifier;

	public int getStreamIdentifier() {
		return streamIdentifier;
	}

	public void setStreamIdentifier(int streamIdentifier) {
		this.streamIdentifier = streamIdentifier;
	}

	public byte getFlags() {
		return flags;
	}
}
