package com.gifisan.nio.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ClientEndPointImpl implements ClientEndPoint {

	SocketChannel	channel	= null;

	public ClientEndPointImpl(SocketChannel channel) {
		this.channel = channel;
	}

	public void close() throws IOException {
		channel.close();
	}

	public void write(byte b) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(1);
		buffer.put(b);
		write(buffer);
	}

	public void write(byte[] bytes) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		write(buffer);
	}

	public void write(byte[] bytes, int offset, int length) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, length);
		write(buffer);
	}

	public int read(ByteBuffer buffer) throws IOException {
		try {
			return this.channel.read(buffer);
		} catch (IOException e) {
			throw new NIOException(e.getMessage(), e);
		}
	}

	public void write(ByteBuffer buffer) throws IOException {
		SocketChannel channel = this.channel;
		try {
			int length = buffer.limit();
			int _length = channel.write(buffer);
			for (;length > _length;) {
				_length += channel.write(buffer);
			}
		} catch (IOException e) {
			throw new NIOException(e.getMessage(), e);
		}
	}
	
	public void register(Selector selector,int option) throws ClosedChannelException{
		channel.register(selector, option);
	}

}
