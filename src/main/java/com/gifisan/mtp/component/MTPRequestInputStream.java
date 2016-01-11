package com.gifisan.mtp.component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.gifisan.mtp.server.EndPoint;
import com.gifisan.mtp.server.RequestInputStream;

public class MTPRequestInputStream extends InputStream implements RequestInputStream{
	
	private static byte [] empty 	= {};
	private int avaiable 			= 0;
	private int BLOCK 				= 0;
	private ByteBuffer buffer 		= null;
	private EndPoint endPoint 		= null;
	private int position 			= 0;
	
//	private ByteBuffer buffer = ByteBuffer.allocate(BLOCK);
	
	public MTPRequestInputStream(EndPoint endPoint,int avaiable) {
		this.endPoint = endPoint;
		this.avaiable = avaiable;
	}

	public int available() throws IOException {
		return this.avaiable;
	}
	
	public void close() throws IOException {
		
	}
	
	public boolean complete() {
		return avaiable == 0 || position >= avaiable;
	}

	public void mark(int readlimit) {
		//throw new IOException("not support");
	}

	public boolean markSupported() {
		//throw new IOException("not support");
		return false;
	}

	public int read() throws IOException {
		throw new IOException("not support");
	}

	/**
	 * use public byte [] read(int length) throws IOException
	 */
	@Deprecated
	public int read(byte[] bytes) throws IOException {
		
		if (complete()) {
			return -1;
		}
		
		int limit = bytes.length;
		
		if (position + limit > avaiable) {
			limit = avaiable - position;
		}
		
		if (limit == BLOCK) {
			buffer.clear();
		}else if (limit > BLOCK) {
			BLOCK = limit;
			buffer = ByteBuffer.allocate(BLOCK);
		}else{
			buffer.clear();
			buffer.limit(limit);
		}
		
		int length = endPoint.read(buffer);
		for(;length < limit;){
			int _length = endPoint.read(buffer);
			length += _length;
			for(;_length > 0;){
				_length = endPoint.read(buffer);
				_length += _length;
			}
			if (_length == limit) {
				break;
			}
			
			synchronized (endPoint) {
				try {
					endPoint.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		this.position += length;
		buffer.flip();
		buffer.get(bytes,0,length);
		return length;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		throw new IOException("not support");
	}

	public byte [] read(int length) throws IOException {
		if (complete()) {
			return empty;
		}
		
		int limit = length;
		
		if (position + limit > avaiable) {
			limit = avaiable - position;
		}
		
		if (limit == BLOCK) {
			buffer.clear();
		}else{
			BLOCK = limit;
			buffer = ByteBuffer.allocate(BLOCK);
		}
		
		int _length = endPoint.read(buffer);
		for(;_length < limit;){
			int __length = endPoint.read(buffer);
			_length += __length;
			for(;__length > 0;){
				__length = endPoint.read(buffer);
				_length += __length;
			}
			if (_length == limit) {
				break;
			}
			synchronized (endPoint) {
				try {
					endPoint.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		this.position += limit;
		return buffer.array();
	}

	public void reset() throws IOException {
		throw new IOException("not support");
	}

	public long skip(long n) throws IOException {
		throw new IOException("not support");
	}
	

}
