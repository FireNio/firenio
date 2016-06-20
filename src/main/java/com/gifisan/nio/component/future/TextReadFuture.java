package com.gifisan.nio.component.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.TCPEndPoint;

public class TextReadFuture extends AbstractReadFuture implements IOReadFuture {

	public TextReadFuture(TCPEndPoint endPoint, Integer futureID, String serviceName) {
		super(endPoint, futureID, serviceName);
	}

	public TextReadFuture(TCPEndPoint endPoint, ByteBuffer header) {
		super(endPoint, header);
	}

	protected boolean doRead(TCPEndPoint endPoint) throws IOException {
		return true;
	}

}
