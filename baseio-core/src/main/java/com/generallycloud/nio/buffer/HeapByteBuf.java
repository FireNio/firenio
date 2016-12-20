/*
 * Copyright 2015 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.nio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.component.SocketChannel;

public class HeapByteBuf extends AbstractByteBuf {

	protected byte[]	memory;

	public HeapByteBuf(ByteBufAllocator allocator, byte[] memory) {
		super(allocator);
		this.memory = memory;
	}

	@Override
	public byte[] array() {
		return memory;
	}

	@Override
	protected AbstractByteBuf newByteBuf() {
		return new HeapByteBuf(allocator, memory);
	}

	@Override
	public byte getByte(int index) {
		return memory[ix(index)];
	}

	@Override
	public void get(byte[] dst, int offset, int length) {
		System.arraycopy(memory, ix(position), dst, offset, length);
		this.position += length;
	}

	@Override
	public int getInt() {
		int v = MathUtil.byte2Int(memory, ix(position));
		this.position += 4;
		return v;
	}

	@Override
	public int getInt(int index) {
		return MathUtil.byte2Int(memory, ix(index));
	}

	@Override
	public long getLong() {
		long v = MathUtil.byte2Long(memory, ix(position));
		this.position += 8;
		return v;
	}

	@Override
	public long getLong(int index) {
		return MathUtil.byte2Long(memory, ix(index));
	}

	@Override
	public boolean hasArray() {
		return true;
	}

	@Override
	public void put(byte[] src, int offset, int length) {
		System.arraycopy(src, offset, memory, ix(position), length);
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

			buffer.get(memory, ix(position), remaining);

			this.position(this.limit);

			return remaining;
		} else {

			buffer.get(memory, ix(position), srcRemaining);

			this.position(this.position + srcRemaining);

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
		return memory[ix(position++)];
	}

	@Override
	public int forEachByte(int index, int length, ByteProcessor processor) {

		byte[] array = memory;

		int start = ix(index);

		int end = start + length;

		try {

			for (int i = start; i < end; i++) {

				if (!processor.process(array[i])) {

					return i - start;
				}

			}

		} catch (Exception e) {
		}

		return -1;
	}

	@Override
	public int forEachByteDesc(int index, int length, ByteProcessor processor) {

		byte[] array = memory;

		int start = ix(index);

		int end = start + length;

		try {

			for (int i = end; i >= start; i--) {

				if (!processor.process(array[i])) {

					return i - start;
				}

			}

		} catch (Exception e) {
		}

		return -1;
	}

	@Override
	public void putByte(byte b) {
		memory[ix(position++)] = b;
	}

	@Override
	public int read(ByteBuf buf) {

		int srcRemaining = buf.remaining();

		if (srcRemaining == 0) {
			return 0;
		}

		int remaining = this.remaining();

		if (remaining <= srcRemaining) {

			buf.get(memory, ix(position), remaining);

			position(limit);

			return remaining;
		} else {

			buf.get(memory, ix(position), srcRemaining);

			position(position + srcRemaining);

			return srcRemaining;
		}
	}

	@Override
	public int getIntLE() {
		int v = MathUtil.byte2IntLE(memory, ix(position));
		this.position += 4;
		return v;
	}

	@Override
	public int getIntLE(int offset) {
		return MathUtil.byte2IntLE(memory, ix(offset));
	}

	@Override
	public long getLongLE() {
		long v = MathUtil.byte2LongLE(memory, ix(position));
		this.position += 8;
		return v;
	}

	@Override
	public long getLongLE(int index) {
		return MathUtil.byte2LongLE(memory, ix(index));
	}

	@Override
	public short getShort() {
		short v = MathUtil.byte2Short(memory, ix(position));
		this.position += 2;
		return v;
	}

	@Override
	public short getShort(int index) {
		return MathUtil.byte2Short(memory, ix(index));
	}

	@Override
	public short getShortLE() {
		short v = MathUtil.byte2ShortLE(memory, ix(position));
		this.position += 2;
		return v;
	}

	@Override
	public short getShortLE(int index) {
		return MathUtil.byte2ShortLE(memory, ix(index));
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
		long v = MathUtil.byte2UnsignedInt(memory, ix(position));
		this.position += 4;
		return v;
	}

	@Override
	public long getUnsignedInt(int index) {
		return MathUtil.byte2UnsignedInt(memory, ix(index));
	}

	@Override
	public long getUnsignedIntLE() {
		long v = MathUtil.byte2UnsignedIntLE(memory, ix(position));
		this.position += 4;
		return v;
	}

	@Override
	public long getUnsignedIntLE(int index) {
		return MathUtil.byte2UnsignedIntLE(memory, ix(index));
	}

	@Override
	public int getUnsignedShort() {
		int v = MathUtil.byte2UnsignedShort(memory, ix(position));
		this.position += 2;
		return v;
	}

	@Override
	public int getUnsignedShort(int index) {
		return MathUtil.byte2UnsignedShort(memory, ix(index));
	}

	@Override
	public int getUnsignedShortLE() {
		int v = MathUtil.byte2UnsignedShortLE(memory, ix(position));
		this.position += 2;
		return v;
	}

	@Override
	public int getUnsignedShortLE(int index) {
		return MathUtil.byte2UnsignedShortLE(memory, ix(index));
	}

	@Override
	protected ByteBuffer getNioBuffer() {
		if (nioBuffer == null) {
			nioBuffer = ByteBuffer.wrap(memory, offset, capacity);
		}
		return nioBuffer;
	}

	@Override
	public void putShort(short value) {
		MathUtil.short2Byte(memory, value, ix(position));
		position +=2;
	}

	@Override
	public void putShortLE(short value) {
		MathUtil.short2ByteLE(memory, value, ix(position));
		position +=2;
	}

	@Override
	public void putUnsignedShort(int value) {
		MathUtil.unsignedShort2Byte(memory, value, ix(position));
		position +=2;
	}

	@Override
	public void putUnsignedShortLE(int value) {
		MathUtil.unsignedShort2ByteLE(memory, value, ix(position));
		position +=2;
	}

	@Override
	public void putInt(int value) {
		MathUtil.int2Byte(memory, value, ix(position));
		position +=4;
	}

	@Override
	public void putIntLE(int value) {
		MathUtil.int2ByteLE(memory, value, ix(position));
		position +=4;
	}

	@Override
	public void putUnsignedInt(long value) {
		MathUtil.unsignedInt2Byte(memory, value, ix(position));
		position +=4;
	}

	@Override
	public void putUnsignedIntLE(long value) {
		MathUtil.unsignedInt2ByteLE(memory, value, ix(position));
		position +=4;
	}

	@Override
	public void putLong(long value) {
		MathUtil.long2Byte(memory, value, ix(position));
		position +=8;
	}

	@Override
	public void putLongLE(long value) {
		MathUtil.long2ByteLE(memory, value, ix(position));
		position +=8;
	}
	
}
