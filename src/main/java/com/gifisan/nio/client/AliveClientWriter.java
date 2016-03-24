package com.gifisan.nio.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.locks.ReentrantLock;

public class AliveClientWriter extends NormalClientWriter implements ClientWriter {

	private byte[]			beat	= { 3 };
	private ReentrantLock	lock	= new ReentrantLock();

	public void writeStream(ClientEndPoint endPoint, InputStream inputStream, int block) throws IOException {

		lock.lock();
		try {
			super.writeStream(endPoint, inputStream, block);
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
