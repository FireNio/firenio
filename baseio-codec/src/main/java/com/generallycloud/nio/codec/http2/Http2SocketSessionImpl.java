package com.generallycloud.nio.codec.http2;

import com.generallycloud.nio.codec.http2.future.Http2FrameHeader;
import com.generallycloud.nio.codec.http2.future.Http2FrameType;
import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.component.UnsafeSessionImpl;

public class Http2SocketSessionImpl extends UnsafeSessionImpl implements Http2SocketSession {

	public Http2SocketSessionImpl(SocketChannel channel, Integer sessionID) {
		super(channel, sessionID);
	}

	private Http2FrameHeader	lastReadFrameHeader;

	private long[]			settings		= new long[] { 0, 4096, 1, 128, 65535, 16384, 0 };

	private Http2FrameType	frameWillBeRead	= Http2FrameType.FRAME_TYPE_PREFACE;

	public Http2FrameHeader getLastReadFrameHeader() {
		return lastReadFrameHeader;
	}

	public void setLastReadFrameHeader(Http2FrameHeader lastReadFrameHeader) {
		this.lastReadFrameHeader = lastReadFrameHeader;
	}

	public Http2FrameType getFrameWillBeRead() {
		return frameWillBeRead;
	}

	public void setFrameWillBeRead(Http2FrameType frameWillBeRead) {
		this.frameWillBeRead = frameWillBeRead;
	}

	public void setFrameWillBeRead(int frameWillBeRead) {
		this.frameWillBeRead = Http2FrameType.getValue(frameWillBeRead);
	}

	public long getSettings(int i) {
		return settings[i];
	}

	public void setSettings(int key, long value) {
		settings[key] = value;
	}

	public long[] getSettings() {
		return settings;
	}

}
