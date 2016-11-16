package com.generallycloud.nio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.SocketChannel;

public class DuplicateByteBuf implements ByteBuf {

	private ByteBuf	byteBuf;

	private ByteBuf	prototype;
	
	public DuplicateByteBuf(ByteBuf byteBuf, ByteBuf prototype) {
		this.byteBuf = byteBuf;
		this.prototype = prototype;
	}

	private ByteBuf unwrap() {
		return byteBuf;
	}

	public void release() {
		ReleaseUtil.release(prototype);
	}

	public byte[] array() {
		return unwrap().array();
	}

	public int capacity() {
		return unwrap().capacity();
	}

	public ByteBuf clear() {
		return unwrap().clear();
	}

	public ByteBuf duplicate() {
		return prototype.duplicate();
	}

	public ByteBuf flip() {
		return unwrap().flip();
	}

	public int forEachByte(ByteProcessor processor) {
		return unwrap().forEachByte(processor);
	}

	public int forEachByte(int index, int length, ByteProcessor processor) {
		return unwrap().forEachByte(index, length, processor);
	}

	public int forEachByteDesc(ByteProcessor processor) {
		return unwrap().forEachByteDesc(processor);
	}

	public int forEachByteDesc(int index, int length, ByteProcessor processor) {
		return unwrap().forEachByteDesc(index, length, processor);
	}

	public void get(byte[] dst) {
		unwrap().get(dst);
	}

	public void get(byte[] dst, int offset, int length) {
		unwrap().get(dst, offset, length);
	}

	public byte getByte() {
		return unwrap().getByte();
	}

	public byte getByte(int index) {
		return unwrap().getByte(index);
	}

	public byte[] getBytes() {
		return unwrap().getBytes();
	}

	public int getInt() {
		return unwrap().getInt();
	}

	public int getInt(int index) {
		return unwrap().getInt(index);
	}

	public int getIntLE() {
		return unwrap().getIntLE();
	}

	public int getIntLE(int index) {
		return unwrap().getIntLE(index);
	}

	public long getLong() {
		return unwrap().getLong();
	}

	public long getLong(int index) {
		return unwrap().getLong(index);
	}

	public long getLongLE() {
		return unwrap().getLongLE();
	}

	public long getLongLE(int index) {
		return unwrap().getLongLE(index);
	}

	public short getShort() {
		return unwrap().getShort();
	}

	public short getShort(int index) {
		return unwrap().getShort(index);
	}

	public short getShortLE() {
		return unwrap().getShortLE();
	}

	public short getShortLE(int index) {
		return unwrap().getShortLE(index);
	}

	public short getUnsignedByte() {
		return unwrap().getUnsignedByte();
	}

	public short getUnsignedByte(int index) {
		return unwrap().getUnsignedByte();
	}

	public long getUnsignedInt() {
		return unwrap().getUnsignedInt();
	}

	public long getUnsignedInt(int index) {
		return unwrap().getUnsignedInt(index);
	}

	public long getUnsignedIntLE() {
		return unwrap().getUnsignedIntLE();
	}

	public long getUnsignedIntLE(int index) {
		return unwrap().getUnsignedIntLE(index);
	}

	public int getUnsignedShort() {
		return unwrap().getUnsignedShort();
	}

	public int getUnsignedShort(int index) {
		return unwrap().getUnsignedShort(index);
	}

	public int getUnsignedShortLE() {
		return unwrap().getUnsignedShortLE();
	}

	public int getUnsignedShortLE(int index) {
		return unwrap().getUnsignedShortLE(index);
	}

	public boolean hasArray() {
		return unwrap().hasArray();
	}

	public boolean hasRemaining() {
		return unwrap().hasRemaining();
	}

	public int limit() {
		return unwrap().limit();
	}

	public ByteBuf limit(int limit) {
		return unwrap().limit(limit);
	}

	public ByteBuffer nioBuffer() {
		return unwrap().nioBuffer();
	}

	public int offset() {
		return unwrap().offset();
	}

	public int position() {
		return unwrap().position();
	}

	public ByteBuf position(int position) {
		return unwrap().position(position);
	}

	public void putByte(byte b) {
		unwrap().putByte(b);
	}

	public void put(byte[] src) {
		unwrap().put(src);
	}

	public void put(byte[] src, int offset, int length) {
		unwrap().put(src, offset, length);
	}

	public void putShort(short value) {
		unwrap().putShort(value);
	}

	public void putShortLE(short value) {
		unwrap().putShortLE(value);
	}

	public void putUnsignedShort(int value) {
		unwrap().putUnsignedShort(value);
	}

	public void putUnsignedShortLE(int value) {
		unwrap().putUnsignedShortLE(value);
	}

	public void putInt(int value) {
		unwrap().putInt(value);
	}

	public void putIntLE(int value) {
		unwrap().putIntLE(value);
	}

	public void putUnsignedInt(long value) {
		unwrap().putUnsignedInt(value);
	}

	public void putUnsignedIntLE(long value) {
		unwrap().putUnsignedIntLE(value);
	}

	public void putLong(long value) {
		unwrap().putLong(value);
	}

	public void putLongLE(long value) {
		unwrap().putLongLE(value);
	}

	public int read(ByteBuf buf) throws IOException {
		return unwrap().read(buf);
	}

	public int read(ByteBuffer buffer) throws IOException {
		return unwrap().read(buffer);
	}

	public int read(SocketChannel channel) throws IOException {
		return unwrap().read(channel);
	}

	public int remaining() {
		return unwrap().remaining();
	}

	public void skipBytes(int length) {
		unwrap().skipBytes(length);
	}

	public int write(SocketChannel channel) throws IOException {
		return unwrap().write(channel);
	}
}
