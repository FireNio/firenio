package com.generallycloud.nio.codec.http2;

import com.generallycloud.nio.codec.http2.future.Http2FrameHeader;
import com.generallycloud.nio.codec.http2.future.Http2FrameType;
import com.generallycloud.nio.component.SocketSession;

public interface Http2SocketSession extends SocketSession {

	public abstract int getSettings(int i);

	public abstract int[] getSettings();

	public abstract void setSettings(int key, int value);

	public abstract Http2FrameHeader getLastReadFrameHeader();

	public abstract void setLastReadFrameHeader(Http2FrameHeader lastReadFrameHeader);

	public abstract Http2FrameType getFrameWillBeRead();

	public abstract void setFrameWillBeRead(Http2FrameType frameWillBeRead);

	public abstract void setFrameWillBeRead(int frameWillBeRead);

}
