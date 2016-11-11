package com.generallycloud.nio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.generallycloud.nio.component.SocketChannel;

public class DirectByteBuf extends AbstractByteBuf {

	protected ByteBuffer	memory;

	protected DirectByteBuf(ByteBuffer memory) {
		super(memory.capacity());
		this.memory = memory;
	}

	public DirectByteBuf(ByteBufAllocator allocator, ByteBuffer memory) {
		this(allocator, memory, new ReferenceCount());
	}

	public DirectByteBuf(ByteBufAllocator allocator, ByteBuffer memory, ReferenceCount referenceCount) {
		super(allocator, referenceCount);
		this.memory = memory;
	}

	public byte[] array() {
		throw new UnsupportedOperationException();
	}

	protected AbstractByteBuf newByteBuf() {
		return new DirectByteBuf(allocator, memory.duplicate(), referenceCount);
	}

	public byte getByte(int index) {
		return memory.get(ix(index));
	}

	public void get(byte[] dst, int offset, int length) {
		memory.get(dst, offset, length);
		this.position += length;
	}

	public int getInt() {
		int v = memory.getInt();
		this.position += 4;
		return v;
	}

	public int getInt(int index) {
		return memory.getInt(ix(index));
	}

	public long getLong() {
		long v = memory.getLong();
		this.position += 8;
		return v;
	}

	public long getLong(int index) {
		return memory.getLong(ix(index));
	}

	public boolean hasArray() {
		return false;
	}

	public void put(byte[] src, int offset, int length) {
		memory.put(src, offset, length);
		this.position += length;
	}

	public int read(ByteBuffer buffer) throws IOException {

		int srcRemaining = buffer.remaining();

		if (srcRemaining == 0) {
			return 0;
		}

		int remaining = this.remaining();

		if (remaining <= srcRemaining) {

			ByteBuffer buf = this.memory;

			for (int i = 0; i < remaining; i++) {

				buf.put(buffer.get());
			}

			this.position(this.limit);

			return remaining;
		} else {

			ByteBuffer buf = this.memory;

			for (int i = 0; i < srcRemaining; i++) {

				buf.put(buffer.get());
			}

			this.skipBytes(srcRemaining);

			return srcRemaining;
		}
	}

	public int write(SocketChannel channel) throws IOException {

		int length = channel.write(nioBuffer);

		if (length > 0) {

			position += length;

			channel.upNetworkState();

		} else {

			channel.downNetworkState();
		}

		return length;
	}

	public byte getByte() {
		return memory.get();
	}

	public int forEachByte(int index, int length, ByteProcessor processor) {

		int start = ix(index);

		int end = start + length;

		try {

			for (int i = start; i < end; i++) {

				if (!processor.process(getByte(i))) {

					return i - start;
				}

			}

		} catch (Exception e) {
		}

		return -1;
	}

	public int forEachByteDesc(int index, int length, ByteProcessor processor) {

		int start = ix(index);

		int end = start + length;

		try {

			for (int i = end; i >= start; i--) {

				if (!processor.process(getByte(i))) {

					return i - start;
				}

			}

		} catch (Exception e) {
		}

		return -1;
	}

	public void putByte(byte b) {
		memory.put(b);
	}

	public int read(ByteBuf buf) throws IOException {

		int srcRemaining = buf.remaining();

		if (srcRemaining == 0) {
			return 0;
		}

		int remaining = this.remaining();

		if (remaining <= srcRemaining) {

			if (buf.hasArray()) {

				this.put(buf.array(), buf.offset() + buf.position(), remaining);
			} else {

				ByteBuffer _this = this.memory;

				for (int i = 0; i < remaining; i++) {

					_this.put(buf.getByte());
				}
			}

			this.position(this.limit);

			return remaining;
		} else {

			if (buf.hasArray()) {

				this.put(buf.array(), buf.offset() + buf.position(), srcRemaining);

			} else {

				ByteBuffer _this = this.memory;

				for (int i = 0; i < srcRemaining; i++) {

					_this.put(buf.getByte());
				}
			}

			this.skipBytes(srcRemaining);

			return srcRemaining;
		}
	}

	public int getIntLE() {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		int v = memory.getInt();
		memory.order(ByteOrder.BIG_ENDIAN);
		this.position += 4;
		return v;
	}

	public int getIntLE(int index) {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		int v = memory.getInt(ix(index));
		memory.order(ByteOrder.BIG_ENDIAN);
		return v;
	}

	public long getLongLE() {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		long v = memory.getLong();
		memory.order(ByteOrder.BIG_ENDIAN);
		this.position += 8;
		return v;
	}

	public long getLongLE(int index) {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		long v = memory.getLong(ix(index));
		memory.order(ByteOrder.BIG_ENDIAN);
		return v;
	}

	public int getShort() {
		int v = memory.getShort();
		this.position += 2;
		return v;
	}

	public int getShort(int index) {
		return memory.getShort(ix(index));
	}

	public int getShortLE() {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		int v = memory.getShort();
		memory.order(ByteOrder.BIG_ENDIAN);
		this.position += 2;
		return v;
	}

	public int getShortLE(int index) {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		int v = memory.getShort(ix(index));
		memory.order(ByteOrder.BIG_ENDIAN);
		return v;
	}

	public int getUnsignedByte() {
		return getByte() & 0xff;
	}

	public int getUnsignedByte(int index) {
		return getByte(index) & 0xff;
	}

	public long getUnsignedInt() {
		long v = toUnsignedInt(memory.getInt());
		this.position += 4;
		return v;
	}

	private long toUnsignedInt(int value) {
		if (value < 0) {
			return value & 0xffffffffffffffffL;
		}
		return value;
	}

	public long getUnsignedInt(int index) {
		return toUnsignedInt(memory.getInt(ix(index)));
	}

	public long getUnsignedIntLE() {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		long v = toUnsignedInt(memory.getInt());
		memory.order(ByteOrder.BIG_ENDIAN);
		this.position += 4;
		return v;
	}

	public long getUnsignedIntLE(int index) {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		long v = toUnsignedInt(memory.getInt(ix(index)));
		memory.order(ByteOrder.BIG_ENDIAN);
		return v;
	}

	public int getUnsignedShort() {
		int v = memory.getShort() & 0xffff;
		this.position += 2;
		return v;
	}

	public int getUnsignedShort(int index) {
		return memory.getShort(ix(index) & 0xffff);
	}

	public int getUnsignedShortLE() {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		int v = memory.getShort() & 0xffff;
		memory.order(ByteOrder.BIG_ENDIAN);
		this.position += 2;
		return v;
	}

	public int getUnsignedShortLE(int index) {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		int v = memory.getShort(ix(index)) & 0xff;
		memory.order(ByteOrder.BIG_ENDIAN);
		return v;
	}

	protected ByteBuffer getNioBuffer() {
		return nioBuffer;
	}

	public void putShort(short value) {
		memory.putShort(value);
		position += 2;
	}

	public void putShortLE(short value) {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		memory.putShort(value);
		memory.order(ByteOrder.BIG_ENDIAN);
		position += 2;
	}

	public void putUnsignedShort(int value) {
		byte b1 = (byte) (value & 0xff);
		byte b0 = (byte) (value >> 8 * 1);
		memory.put(b0);
		memory.put(b1);
		position += 2;
	}

	public void putUnsignedShortLE(int value) {
		byte b0 = (byte) (value & 0xff);
		byte b1 = (byte) (value >> 8 * 1);
		memory.put(b0);
		memory.put(b1);
		position += 2;
	}

	public void putInt(int value) {
		memory.putInt(value);
		position += 4;
	}

	public void putIntLE(int value) {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		memory.putInt(value);
		memory.order(ByteOrder.BIG_ENDIAN);
		position += 4;
	}

	public void putUnsignedInt(long value) {
		byte b3 = (byte) ((value & 0xff));
		byte b2 = (byte) ((value >> 8 * 1) & 0xff);
		byte b1 = (byte) ((value >> 8 * 2) & 0xff);
		byte b0 = (byte) ((value >> 8 * 3));
		memory.put(b0);
		memory.put(b1);
		memory.put(b2);
		memory.put(b3);
		position += 4;
	}

	public void putUnsignedIntLE(long value) {
		byte b0 = (byte) ((value & 0xff));
		byte b1 = (byte) ((value >> 8 * 1) & 0xff);
		byte b2 = (byte) ((value >> 8 * 2) & 0xff);
		byte b3 = (byte) ((value >> 8 * 3));
		memory.put(b0);
		memory.put(b1);
		memory.put(b2);
		memory.put(b3);
		position += 4;
	}

	public void putLong(long value) {
		memory.putLong(value);
		position += 8;
	}

	public void putLongLE(long value) {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		memory.putLong(value);
		memory.order(ByteOrder.BIG_ENDIAN);
		position += 8;
	}

}
