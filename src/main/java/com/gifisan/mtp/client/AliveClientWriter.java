package com.gifisan.mtp.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

public class AliveClientWriter extends NormalClientWriter implements ClientWriter {

	private byte[]			beat	= { 3 };
	private ReentrantLock	lock	= new ReentrantLock();

	public void writeStream(ClientEndPoint endPoint, InputStream inputStream, ByteBuffer header, int block)
			throws IOException {

		lock.lock();
		try {
			super.writeStream(endPoint, inputStream, header, block);
		} finally {
			lock.unlock();
		}

	}

	public void writeBeat(ClientEndPoint endPoint) throws IOException {

		lock.lock();
		try {
			endPoint.write(beat);
		} finally {
			lock.unlock();
		}
	}

}
