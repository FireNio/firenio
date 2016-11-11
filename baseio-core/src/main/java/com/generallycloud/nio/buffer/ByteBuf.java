package com.generallycloud.nio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.Releasable;
import com.generallycloud.nio.component.SocketChannel;

public interface ByteBuf extends Releasable {

	public abstract byte[] array();

	public abstract int capacity();

	public abstract ByteBuf clear();

	public abstract ByteBuf duplicate();

	public abstract ByteBuf flip();

	public abstract int forEachByte(ByteProcessor processor);

	public abstract int forEachByte(int index, int length, ByteProcessor processor);

	public abstract int forEachByteDesc(ByteProcessor processor);

	public abstract int forEachByteDesc(int index, int length, ByteProcessor processor);

	public abstract void get(byte[] dst);

	public abstract void get(byte[] dst, int offset, int length);

	public abstract byte getByte();

	public abstract byte getByte(int index);
	
	public abstract byte[] getBytes();

	public abstract int getInt();
	
	public abstract int getInt(int index);

	public abstract int getIntLE();
	
	public abstract int getIntLE(int index);
	
	public abstract long getLong();
	
	public abstract long getLong(int index);
	
	public abstract long getLongLE();
	
	public abstract long getLongLE(int index);
	
	public abstract int getShort();

	public abstract int getShort(int index);

	public abstract int getShortLE();

	public abstract int getShortLE(int index);
	
	public abstract int getUnsignedByte();
	
	public abstract int getUnsignedByte(int index);
	
	public abstract long getUnsignedInt();
	
	public abstract long getUnsignedInt(int index);
	
	public abstract long getUnsignedIntLE();
	
	public abstract long getUnsignedIntLE(int index);
	
	public abstract int getUnsignedShort();

	public abstract int getUnsignedShort(int index);

	public abstract int getUnsignedShortLE();

	public abstract int getUnsignedShortLE(int index);

	public abstract boolean hasArray();

	public abstract boolean hasRemaining();

	public abstract int limit();

	public abstract ByteBuf limit(int limit);
	
	public abstract ByteBuffer nioBuffer();

	public abstract int offset();

	public abstract int position();

	public abstract ByteBuf position(int position);

	public abstract void putByte(byte b);
	
	public abstract void put(byte[] src);

	public abstract void put(byte[] src, int offset, int length);

	public abstract void putShort(short value);
	
	public abstract void putShortLE(short value);
	
	public abstract void putUnsignedShort(int value);
	
	public abstract void putUnsignedShortLE(int value);
	
	public abstract void putInt(int value);
	
	public abstract void putIntLE(int value);
	
	public abstract void putUnsignedInt(long value);
	
	public abstract void putUnsignedIntLE(long value);

	public abstract void putLong(long value);
	
	public abstract void putLongLE(long value);
	
	public abstract int read(ByteBuf buf) throws IOException;

	public abstract int read(ByteBuffer buffer) throws IOException;

	// 往buffer中write
	public abstract int read(SocketChannel channel) throws IOException;

	public abstract int remaining();

	public abstract void skipBytes(int length);
	
	// 往buffer中read
	public abstract int write(SocketChannel channel) throws IOException;

}