package com.gifisan.nio.component.protocol.nio.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;

public class TextReadFuture extends AbstractNIOReadFuture {

	public TextReadFuture(Session session, Integer futureID, String serviceName) {
		super(session, futureID, serviceName);
	}

	public TextReadFuture(Session session, ByteBuffer header) {
		super(session, header);
	}

	protected TextReadFuture(Session session, boolean isBeatPacket) {
		super(session, isBeatPacket);
	}

	protected boolean doRead(TCPEndPoint endPoint) throws IOException {
		return true;
	}

}
