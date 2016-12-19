package com.generallycloud.nio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.generallycloud.nio.component.SocketChannel;

public class DirectByteBuf extends AbstractByteBuf {

	protected ByteBuffer	memory;
	
	public DirectByteBuf(ByteBufAllocator allocator, ByteBuffer memory) {
		super(allocator);
		this.memory = memory;
	}

	@Override
	public byte[] array() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected AbstractByteBuf newByteBuf() {
		return new DirectByteBuf(allocator, memory.duplicate());
	}

	@Override
	public byte getByte(int index) {
		return memory.get(ix(index));
	}

	@Override
	public void get(byte[] dst, int offset, int length) {
		memory.get(dst, offset, length);
		this.position += length;
	}

	@Override
	public int getInt() {
		int v = memory.getInt();
		this.position += 4;
		return v;
	}

	@Override
	public int getInt(int index) {
		return memory.getInt(ix(index));
	}

	@Override
	public long getLong() {
		long v = memory.getLong();
		this.position += 8;
		return v;
	}

	@Override
	public long getLong(int index) {
		return memory.getLong(ix(index));
	}

	@Override
	public boolean hasArray() {
		return false;
	}

	@Override
	public void put(byte[] src, int offset, int length) {
		memory.put(src, offset, length);
		this.position += length;
	}

	@Override
	public int read(ByteBuffer buffer) {

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

	@Override
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

	@Override
	public byte getByte() {
		return memory.get();
	}

	@Override
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

	@Override
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

	@Override
	public void putByte(byte b) {
		memory.put(b);
	}

	@Override
	public int read(ByteBuf buf) {

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

	@Override
	public int getIntLE() {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		int v = memory.getInt();
		memory.order(ByteOrder.BIG_ENDIAN);
		this.position += 4;
		return v;
	}

	@Override
	public int getIntLE(int index) {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		int v = memory.getInt(ix(index));
		memory.order(ByteOrder.BIG_ENDIAN);
		return v;
	}

	@Override
	public long getLongLE() {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		long v = memory.getLong();
		memory.order(ByteOrder.BIG_ENDIAN);
		this.position += 8;
		return v;
	}

	@Override
	public long getLongLE(int index) {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		long v = memory.getLong(ix(index));
		memory.order(ByteOrder.BIG_ENDIAN);
		return v;
	}

	@Override
	public short getShort() {
		short v = memory.getShort();
		this.position += 2;
		return v;
	}

	@Override
	public short getShort(int index) {
		return memory.getShort(ix(index));
	}

	@Override
	public short getShortLE() {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		short v = memory.getShort();
		memory.order(ByteOrder.BIG_ENDIAN);
		this.position += 2;
		return v;
	}

	@Override
	public short getShortLE(int index) {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		short v = memory.getShort(ix(index));
		memory.order(ByteOrder.BIG_ENDIAN);
		return v;
	}

	@Override
	public short getUnsignedByte() {
		return (short) (getByte() & 0xff);
	}

	@Override
	public short getUnsignedByte(int index) {
		return (short) (getByte(index) & 0xff);
	}

	@Override
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

	@Override
	public long getUnsignedInt(int index) {
		return toUnsignedInt(memory.getInt(ix(index)));
	}

	@Override
	public long getUnsignedIntLE() {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		long v = toUnsignedInt(memory.getInt());
		memory.order(ByteOrder.BIG_ENDIAN);
		this.position += 4;
		return v;
	}

	@Override
	public long getUnsignedIntLE(int index) {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		long v = toUnsignedInt(memory.getInt(ix(index)));
		memory.order(ByteOrder.BIG_ENDIAN);
		return v;
	}

	@Override
	public int getUnsignedShort() {
		int v = memory.getShort() & 0xffff;
		this.position += 2;
		return v;
	}

	@Override
	public int getUnsignedShort(int index) {
		return memory.getShort(ix(index) & 0xffff);
	}

	@Override
	public int getUnsignedShortLE() {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		int v = memory.getShort() & 0xffff;
		memory.order(ByteOrder.BIG_ENDIAN);
		this.position += 2;
		return v;
	}

	@Override
	public int getUnsignedShortLE(int index) {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		int v = memory.getShort(ix(index)) & 0xff;
		memory.order(ByteOrder.BIG_ENDIAN);
		return v;
	}

	@Override
	protected ByteBuffer getNioBuffer() {
		return nioBuffer;
	}

	@Override
	public void putShort(short value) {
		memory.putShort(value);
		position += 2;
	}

	@Override
	public void putShortLE(short value) {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		memory.putShort(value);
		memory.order(ByteOrder.BIG_ENDIAN);
		position += 2;
	}

	@Override
	public void putUnsignedShort(int value) {
		byte b1 = (byte) (value & 0xff);
		byte b0 = (byte) (value >> 8 * 1);
		memory.put(b0);
		memory.put(b1);
		position += 2;
	}

	@Override
	public void putUnsignedShortLE(int value) {
		byte b0 = (byte) (value & 0xff);
		byte b1 = (byte) (value >> 8 * 1);
		memory.put(b0);
		memory.put(b1);
		position += 2;
	}

	@Override
	public void putInt(int value) {
		memory.putInt(value);
		position += 4;
	}

	@Override
	public void putIntLE(int value) {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		memory.putInt(value);
		memory.order(ByteOrder.BIG_ENDIAN);
		position += 4;
	}

	@Override
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

	@Override
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

	@Override
	public void putLong(long value) {
		memory.putLong(value);
		position += 8;
	}

	@Override
	public void putLongLE(long value) {
		memory.order(ByteOrder.LITTLE_ENDIAN);
		memory.putLong(value);
		memory.order(ByteOrder.BIG_ENDIAN);
		position += 8;
	}

}
