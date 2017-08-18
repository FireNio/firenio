/*
 * Copyright 2015-2017 GenerallyCloud.com
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
package com.generallycloud.baseio.buffer;

import java.nio.ByteBuffer;

import com.generallycloud.baseio.common.ReleaseUtil;

//FIXME 需要增加UnsupportedOperation
public class DuplicateByteBuf implements ByteBuf {

    private ByteBuf byteBuf;

    private ByteBuf prototype;

    private boolean released;

    public DuplicateByteBuf(ByteBuf byteBuf, ByteBuf prototype) {
        this.byteBuf = byteBuf;
        this.prototype = prototype;
    }

    private ByteBuf unwrap() {
        return byteBuf;
    }

    @Override
    public void release() {
        if (released) {
            return;
        }
        released = true;
        ReleaseUtil.release(prototype);
    }

    @Override
    public byte[] array() {
        return unwrap().array();
    }

    @Override
    public int capacity() {
        return unwrap().capacity();
    }

    @Override
    public ByteBuf clear() {
        return unwrap().clear();
    }

    @Override
    public ByteBuf duplicate() {
        return prototype.duplicate();
    }

    @Override
    public ByteBuf flip() {
        return unwrap().flip();
    }

    @Override
    public int forEachByte(ByteProcessor processor) {
        return unwrap().forEachByte(processor);
    }

    @Override
    public int forEachByte(int index, int length, ByteProcessor processor) {
        return unwrap().forEachByte(index, length, processor);
    }

    @Override
    public int forEachByteDesc(ByteProcessor processor) {
        return unwrap().forEachByteDesc(processor);
    }

    @Override
    public int forEachByteDesc(int index, int length, ByteProcessor processor) {
        return unwrap().forEachByteDesc(index, length, processor);
    }

    @Override
    public void get(byte[] dst) {
        unwrap().get(dst);
    }

    @Override
    public void get(byte[] dst, int offset, int length) {
        unwrap().get(dst, offset, length);
    }

    @Override
    public byte getByte() {
        return unwrap().getByte();
    }

    @Override
    public byte getByte(int index) {
        return unwrap().getByte(index);
    }

    @Override
    public byte[] getBytes() {
        return unwrap().getBytes();
    }

    @Override
    public int getInt() {
        return unwrap().getInt();
    }

    @Override
    public int getInt(int index) {
        return unwrap().getInt(index);
    }

    @Override
    public int getIntLE() {
        return unwrap().getIntLE();
    }

    @Override
    public int getIntLE(int index) {
        return unwrap().getIntLE(index);
    }

    @Override
    public long getLong() {
        return unwrap().getLong();
    }

    @Override
    public long getLong(int index) {
        return unwrap().getLong(index);
    }

    @Override
    public long getLongLE() {
        return unwrap().getLongLE();
    }

    @Override
    public long getLongLE(int index) {
        return unwrap().getLongLE(index);
    }

    @Override
    public short getShort() {
        return unwrap().getShort();
    }

    @Override
    public short getShort(int index) {
        return unwrap().getShort(index);
    }

    @Override
    public short getShortLE() {
        return unwrap().getShortLE();
    }

    @Override
    public short getShortLE(int index) {
        return unwrap().getShortLE(index);
    }

    @Override
    public short getUnsignedByte() {
        return unwrap().getUnsignedByte();
    }

    @Override
    public short getUnsignedByte(int index) {
        return unwrap().getUnsignedByte();
    }

    @Override
    public long getUnsignedInt() {
        return unwrap().getUnsignedInt();
    }

    @Override
    public long getUnsignedInt(int index) {
        return unwrap().getUnsignedInt(index);
    }

    @Override
    public long getUnsignedIntLE() {
        return unwrap().getUnsignedIntLE();
    }

    @Override
    public long getUnsignedIntLE(int index) {
        return unwrap().getUnsignedIntLE(index);
    }

    @Override
    public int getUnsignedShort() {
        return unwrap().getUnsignedShort();
    }

    @Override
    public int getUnsignedShort(int index) {
        return unwrap().getUnsignedShort(index);
    }

    @Override
    public int getUnsignedShortLE() {
        return unwrap().getUnsignedShortLE();
    }

    @Override
    public int getUnsignedShortLE(int index) {
        return unwrap().getUnsignedShortLE(index);
    }

    @Override
    public boolean hasArray() {
        return unwrap().hasArray();
    }

    @Override
    public boolean hasRemaining() {
        return unwrap().hasRemaining();
    }

    @Override
    public int limit() {
        return unwrap().limit();
    }

    @Override
    public ByteBuf limit(int limit) {
        return unwrap().limit(limit);
    }

    @Override
    public ByteBuffer nioBuffer() {
        return unwrap().nioBuffer();
    }

    @Override
    public int offset() {
        return unwrap().offset();
    }

    @Override
    public int position() {
        return unwrap().position();
    }

    @Override
    public ByteBuf position(int position) {
        return unwrap().position(position);
    }

    @Override
    public void putByte(byte b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(byte[] src) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(byte[] src, int offset, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putShort(short value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putShortLE(short value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putUnsignedShort(int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putUnsignedShortLE(int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putInt(int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putIntLE(int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putUnsignedInt(long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putUnsignedIntLE(long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putLong(long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putLongLE(long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int read(ByteBuf buf) {
        return unwrap().read(buf);
    }

    @Override
    public int read(ByteBuffer buffer) {
        return unwrap().read(buffer);
    }

    @Override
    public int remaining() {
        return unwrap().remaining();
    }

    @Override
    public ByteBuf skipBytes(int length) {
        return unwrap().skipBytes(length);
    }

    @Override
    public ByteBuf reallocate(int limit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ByteBuf reallocate(int limit, int maxLimit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PooledByteBuf newByteBuf(ByteBufAllocator allocator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ByteBuf reallocate(int limit, boolean copyOld) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ByteBuf reallocate(int limit, int maxLimit, boolean copyOld) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ByteBuf reverse() {
        return unwrap().reverse();
    }

    @Override
    public ByteBuffer getNioBuffer() {
        return unwrap().getNioBuffer();
    }

    @Override
    public boolean isReleased() {
        return released;
    }

}
