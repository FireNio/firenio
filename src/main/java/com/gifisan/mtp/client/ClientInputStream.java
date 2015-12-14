package com.gifisan.mtp.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClientInputStream extends InputStream{
	
	private int avaiable 				= 0;
	private int BLOCK 					= 0;
	private ByteBuffer buffer 			= null;
	private SocketChannel channel 		= null;
	private int position 				= 0;
	
	public ClientInputStream(SocketChannel channel,int avaiable) {
		this.channel = channel;
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
		
		int length = channel.read(buffer);
		while (length < limit) {
			int __length = channel.read(buffer);
			length += __length;
		}
				
		this.position += length;
		buffer.flip();
		buffer.get(bytes,0,length);
		return length;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		throw new IOException("not support");
	}

	public void reset() throws IOException {
		throw new IOException("not support");
	}

	public long skip(long n) throws IOException {
		throw new IOException("not support");
	}
	

}
