package com.generallycloud.nio.codec.http2;

import com.generallycloud.nio.codec.http2.future.Http2FrameHeader;
import com.generallycloud.nio.codec.http2.future.Http2FrameType;
import com.generallycloud.nio.codec.http2.hpack.Http2Headers;
import com.generallycloud.nio.codec.http2.hpack.Http2HeadersImpl;
import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.component.UnsafeSocketSessionImpl;

public class Http2SocketSessionImpl extends UnsafeSocketSessionImpl implements Http2SocketSession {

	public Http2SocketSessionImpl(SocketChannel channel,Integer sessionID) {
		super(channel, sessionID);
	}

	private Http2FrameHeader	lastReadFrameHeader;

	private long[]			settings			= new long[] { 0, 4096, 1, 128, 65535, 16384, 0 };

	private Http2FrameType	frameWillBeRead	= Http2FrameType.FRAME_TYPE_PREFACE;

	private Http2Headers	http2Headers		= new Http2HeadersImpl();

	@Override
	public Http2FrameHeader getLastReadFrameHeader() {
		return lastReadFrameHeader;
	}

	@Override
	public void setLastReadFrameHeader(Http2FrameHeader lastReadFrameHeader) {
		this.lastReadFrameHeader = lastReadFrameHeader;
	}

	@Override
	public Http2FrameType getFrameWillBeRead() {
		return frameWillBeRead;
	}

	@Override
	public void setFrameWillBeRead(Http2FrameType frameWillBeRead) {
		this.frameWillBeRead = frameWillBeRead;
	}

	@Override
	public void setFrameWillBeRead(int frameWillBeRead) {
		this.frameWillBeRead = Http2FrameType.getValue(frameWillBeRead);
	}

	@Override
	public long getSettings(int i) {
		return settings[i];
	}

	@Override
	public void setSettings(int key, long value) {
		settings[key] = value;
	}

	@Override
	public long[] getSettings() {
		return settings;
	}

	@Override
	public Http2Headers getHttp2Headers() {
		return http2Headers;
	}

}
