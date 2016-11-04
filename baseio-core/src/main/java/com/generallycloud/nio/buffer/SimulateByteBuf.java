package com.generallycloud.nio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.component.SocketChannel;

public abstract class SimulateByteBuf implements ByteBuf {

	public ByteBuf duplicate() {

		return null;
	}

	public int remaining() {

		return 0;
	}

	public int position() {

		return 0;
	}

	public ByteBuf position(int position) {

		return null;
	}

	public int limit() {

		return 0;
	}

	public ByteBuf limit(int limit) {

		return null;
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

		return null;
	}

	public ByteBuf clear() {

		return null;
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

		return null;
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

	public byte get() {

		return 0;
	}

	public int forEachByte(ByteProcessor processor) {

		return 0;
	}

	public int forEachByte(int index, int length, ByteProcessor processor) {
		return 0;
	}

	public int forEachByteDesc(ByteProcessor processor) {
		return 0;
	}

	public int forEachByteDesc(int index, int length, ByteProcessor processor) {
		return 0;
	}

	public void skipBytes(int length) {

	}

	public void put(byte b) {

	}

}
