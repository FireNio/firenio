package com.gifisan.nio.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class EndPointInputStream extends InputStream {

	private int				avaiable	= 0;
	private int				position	= 0;
	private ClientEndPoint		endPoint	= null;
	private Condition			complete	= null;
	private ReentrantLock		lock		= null;

	public EndPointInputStream(ClientEndPoint endPoint, int avaiable) {
		this.endPoint = endPoint;
		this.avaiable = avaiable;
	}

	public int available() throws IOException {
		return this.avaiable;
	}

	public boolean complete() {
		return avaiable == 0 || position >= avaiable;
	}
	public ByteBuffer read(int limit) throws IOException{
		ByteBuffer buffer = ByteBuffer.allocate(limit);
		read(buffer);
		return buffer;
	}

	public int read(ByteBuffer buffer) throws IOException {
		if (complete()) {
			return 0;
		}

		int limit = buffer.capacity();

		if (position + limit > avaiable) {

			limit = avaiable - position;

			buffer.limit(limit);
		}

		int _length = endPoint.read(buffer);

		this.position += _length;

		return _length;
	}

	public int read() throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(1);
		endPoint.read(buffer);
		return buffer.limit();
	}

	public void close() {
		
		ReentrantLock lock = this.lock;
		
		if (lock == null) {
			return;
		}
		
		lock.lock();
		
		complete.signal();
		
		lock.unlock();
		
	}
	
	protected void setLock(ReentrantLock lock,Condition condition){
		this.lock = lock;
		this.complete = condition;
	}

}
