package com.generallycloud.nio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.Releasable;
import com.generallycloud.nio.component.SocketChannel;

public interface ByteBuf extends Releasable {

	public static final int			UNIT_CAPACITY	= 12;

	public abstract ByteBuf duplicate();

	public abstract int remaining();

	public abstract int position();

	public abstract ByteBuf position(int position);

	public abstract int limit();

	public abstract ByteBuf limit(int limit);

	public abstract int capacity();

	public abstract boolean hasRemaining();

	public abstract boolean hasArray();

	public abstract byte[] array();

	public abstract ByteBuf flip();

	public abstract ByteBuf clear();

	public abstract int offset();

	public abstract byte get(int index);

	public abstract int getInt();

	public abstract long getLong();

	public abstract int getInt(int offset);

	public abstract long getLong(int offset);

	public abstract void get(byte[] dst);

	public abstract byte[] getBytes();

	public abstract void get(byte[] dst, int offset, int length);

	public abstract ByteBuffer nioBuffer();
	
	public abstract void put(byte[] src);

	public abstract void put(byte[] src, int offset, int length);

	// 往buffer中write
	public abstract int read(SocketChannel channel) throws IOException;

	// 往buffer中read
	public abstract int write(SocketChannel channel) throws IOException;

	public abstract int read(ByteBuffer buffer) throws IOException;
	
	public abstract int read(ByteBuf buf) throws IOException;

	public abstract byte get();

	/**
	 * Iterates over the readable bytes of this buffer with the specified
	 * {@code processor} in ascending order.
	 *
	 * @return {@code -1} if the processor iterated to or beyond the end of the
	 *         readable bytes. The last-visited index If the
	 *         {@link ByteProcessor#process(byte)} returned {@code false}.
	 */
	public abstract int forEachByte(ByteProcessor processor);

	/**
	 * Iterates over the specified area of this buffer with the specified
	 * {@code processor} in ascending order. (i.e. {@code index},
	 * {@code (index + 1)}, .. {@code (index + length - 1)})
	 *
	 * @return {@code -1} if the processor iterated to or beyond the end of the
	 *         specified area. The last-visited index If the
	 *         {@link ByteProcessor#process(byte)} returned {@code false}.
	 */
	public abstract int forEachByte(int index, int length, ByteProcessor processor);

	/**
	 * Iterates over the readable bytes of this buffer with the specified
	 * {@code processor} in descending order.
	 *
	 * @return {@code -1} if the processor iterated to or beyond the beginning
	 *         of the readable bytes. The last-visited index If the
	 *         {@link ByteProcessor#process(byte)} returned {@code false}.
	 */
	public abstract int forEachByteDesc(ByteProcessor processor);

	/**
	 * Iterates over the specified area of this buffer with the specified
	 * {@code processor} in descending order. (i.e.
	 * {@code (index + length - 1)}, {@code (index + length - 2)}, ...
	 * {@code index})
	 *
	 *
	 * @return {@code -1} if the processor iterated to or beyond the beginning
	 *         of the specified area. The last-visited index If the
	 *         {@link ByteProcessor#process(byte)} returned {@code false}.
	 */
	public abstract int forEachByteDesc(int index, int length, ByteProcessor processor);

	public abstract void skipBytes(int length);
	
	public abstract void put(byte b);

}