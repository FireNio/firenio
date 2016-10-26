package com.generallycloud.nio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.component.SocketChannel;

//FIXME throw
public class EmptyMemoryBlockV3 implements ByteBuf{
	
	public static EmptyMemoryBlockV3 EMPTY_BYTEBUF = new EmptyMemoryBlockV3();
	
	private ByteBuffer memory = ByteBuffer.allocate(0);
	
	private EmptyMemoryBlockV3(){}

	public void release() {
		
	}

	public ByteBuf duplicate() {
		return this;
	}

	public int remaining() {
		return 0;
	}

	public int position() {
		return 0;
	}

	public ByteBuf position(int position) {
		return this;
	}

	public int limit() {
		return 0;
	}

	public ByteBuf limit(int limit) {
		return this;
	}

	public int capacity() {
		return 0;
	}

	public boolean hasRemaining() {
		return false;
	}

	public boolean hasArray() {
		return false;
	}

	public byte[] array() {
		return null;
	}

	public ByteBuf flip() {
		return this;
	}

	public ByteBuf clear() {
		return this;
	}

	public int offset() {
		return 0;
	}

	public byte get(int index) {
		return 0;
	}

	public int getInt() {
		return 0;
	}

	public long getLong() {
		return 0;
	}

	public int getInt(int offset) {
		return 0;
	}

	public long getLong(int offset) {
		return 0;
	}

	public void get(byte[] dst) {
		
	}

	public byte[] getBytes() {
		return null;
	}

	public ByteBuffer getMemory() {
		return memory;
	}

	public void get(byte[] dst, int offset, int length) {
		
	}

	public void put(byte[] src) {
		
	}

	public void put(byte[] src, int offset, int length) {
		
	}

	public int read(SocketChannel channel) throws IOException {
		return 0;
	}

	public int write(SocketChannel channel) throws IOException {
		return 0;
	}

	public int read(ByteBuffer buffer) throws IOException {
		return 0;
	}
	
}
