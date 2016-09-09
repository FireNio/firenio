package com.generallycloud.nio.component.protocol.nio.future;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.TCPEndPoint;

public class TextReadFuture extends AbstractNIOReadFuture {

	public TextReadFuture(Session session, Integer futureID, String serviceName) {
		super(session, futureID, serviceName);
	}

	public TextReadFuture(Session session, ByteBuf buffer) throws IOException {
		super(session, buffer);
	}

	protected TextReadFuture(Session session, boolean isBeatPacket) {
		super(session, isBeatPacket);
	}

	protected boolean doRead(TCPEndPoint endPoint) throws IOException {
		return true;
	}

}
