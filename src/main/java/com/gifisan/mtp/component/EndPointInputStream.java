package com.gifisan.mtp.component;

import java.io.IOException;
import java.nio.ByteBuffer;

public class EndPointInputStream implements InputStream {

	private int			avaiable	= 0;
	private EndPoint		endPoint	= null;
	private int			position	= 0;

	public EndPointInputStream(EndPoint endPoint, int avaiable) {
		this.endPoint = endPoint;
		this.avaiable = avaiable;
	}

	public int available() throws IOException {
		return this.avaiable;
	}

	public boolean complete() {
		return avaiable == 0 || position >= avaiable;
	}

	public int read(ByteBuffer buffer) throws IOException {
		if (complete()) {
			return 0;
		}

		int limit = buffer.capacity();

		if (position + limit > avaiable) {
			limit = avaiable - position;
		}

		EndPoint endPoint = this.endPoint;

		int _length = endPoint.read(buffer);
		for (; _length < limit;) {

			int __length = endPoint.read(buffer);
			_length += __length;
			//TODO 处理网速较慢的情况
			for (; __length > 0;) {
				__length = endPoint.read(buffer);
				_length += __length;
			}
			if (_length == limit) {
				break;
			}
			havearest();
		}
		this.position += limit;
		return limit;
	}
	
	public void havearest(){
		synchronized (endPoint) {
			try {
				endPoint.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

}
