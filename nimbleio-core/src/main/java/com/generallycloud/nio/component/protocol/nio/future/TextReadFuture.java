package com.generallycloud.nio.component.protocol.nio.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.TCPEndPoint;

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
