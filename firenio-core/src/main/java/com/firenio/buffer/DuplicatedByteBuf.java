/*
 * Copyright 2015 The FireNio Project
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
package com.firenio.buffer;

import java.nio.ByteBuffer;

/**
 * @author wangkai
 */
final class DuplicatedByteBuf extends ByteBuf {

    private ByteBuffer m;
    private ByteBuf    p;

    DuplicatedByteBuf(ByteBuffer memory, ByteBuf proto, int refCnt) {
        this.p = proto;
        this.m = memory;
        this.referenceCount = refCnt;
    }

    @Override
    public byte getByteAbs(int pos) {
        return p.getByteAbs(pos);
    }

    @Override
    public int absWriteIndex() {
        return p.absWriteIndex();
    }

    @Override
    public ByteBuf absWriteIndex(int limit) {
        throw unsupportedOperationException();
    }

    @Override
    public int absReadIndex() {
        return m.position();
    }

    @Override
    public ByteBuf absReadIndex(int absPos) {
        m.position(absPos);
        return this;
    }

    @Override
    public long address() {
        return p.address();
    }

    @Override
    public byte[] array() {
        return p.array();
    }

    @Override
    public int capacity() {
        return p.capacity();
    }

    @Override
    public ByteBuf clear() {
        m.position(offset());
        m.limit(ix(capacity()));
        return this;
    }

    @Override
    public ByteBuf duplicate() {
        return p.duplicate();
    }

    @Override
    public void expansion(int cap) {
        throw unsupportedOperationException();
    }

    @Override
    protected int get0(ByteBuffer dst, int len) {
        //TODO compete me
        throw unsupportedOperationException();
    }

    @Override
    public byte readByte() {
        return m.get();
    }

    @Override
    public byte getByte(int index) {
        return p.getByte(index);
    }

    @Override
    public void readBytes(byte[] dst, int offset, int length) {
        m.get(dst, offset, length);
    }

    @Override
    public int readInt() {
        return m.getInt();
    }

    @Override
    public int getInt(int index) {
        return p.getInt(index);
    }

    @Override
    public int readIntLE() {
        return Integer.reverseBytes(readInt());
    }

    @Override
    public int getIntLE(int index) {
        return p.getIntLE(index);
    }

    @Override
    public long readLong() {
        return m.getLong();
    }

    @Override
    public long getLong(int index) {
        return p.getLong(index);
    }

    @Override
    public long readLongLE() {
        return Long.reverseBytes(readLong());
    }

    @Override
    public long getLongLE(int index) {
        return p.getLongLE(index);
    }

    @Override
    public ByteBuffer getNioBuffer() {
        return m;
    }

    @Override
    public short readShort() {
        return m.getShort();
    }

    @Override
    public short getShort(int index) {
        return p.getShort(index);
    }

    @Override
    public short readShortLE() {
        return Short.reverseBytes(readShort());
    }

    @Override
    public short getShortLE(int index) {
        return p.getShortLE(index);
    }

    @Override
    public short readUnsignedByte() {
        return (short) (readByte() & 0xff);
    }

    @Override
    public short getUnsignedByte(int index) {
        return p.getUnsignedByte(index);
    }

    @Override
    public long readUnsignedInt() {
        return readInt() & 0xffff_ffff;
    }

    @Override
    public long getUnsignedInt(int index) {
        return p.getUnsignedInt(index);
    }

    @Override
    public long readUnsignedIntLE() {
        return Integer.reverseBytes(readInt()) & 0xffff_ffff;
    }

    @Override
    public long getUnsignedIntLE(int index) {
        return p.getUnsignedIntLE(index);
    }

    @Override
    public int readUnsignedShort() {
        return readShort() & 0xffff;
    }

    @Override
    public int getUnsignedShort(int index) {
        return p.getUnsignedShort(index);
    }

    @Override
    public int readUnsignedShortLE() {
        return Short.reverseBytes(readShort()) & 0xffff;
    }

    @Override
    public int getUnsignedShortLE(int index) {
        return p.getUnsignedShortLE(index);
    }

    @Override
    public boolean hasArray() {
        return m.hasArray();
    }

    @Override
    public boolean hasReadableBytes() {
        return m.hasRemaining();
    }

    @Override
    public int indexOf(byte b, int absPos, int size) {
        return p.indexOf(b, absPos, size);
    }

    @Override
    public boolean isPooled() {
        return p.isPooled();
    }

    @Override
    public int lastIndexOf(byte b, int absPos, int size) {
        return p.lastIndexOf(b, absPos, size);
    }

    @Override
    public int writeIndex() {
        return m.limit() - offset();
    }

    @Override
    public ByteBuf writeIndex(int writeIndex) {
        throw unsupportedOperationException();
    }

    @Override
    public ByteBuf markWriteIndex() {
        throw unsupportedOperationException();
    }

    @Override
    public ByteBuf markReadIndex() {
        throw unsupportedOperationException();
    }

    @Override
    public ByteBuffer nioReadBuffer() {
        return m;
    }

    @Override
    public ByteBuffer nioWriteBuffer() {
        throw unsupportedOperationException();
    }

    @Override
    public int readIndex() {
        return m.position() - offset();
    }

    @Override
    public ByteBuf readIndex(int readIndex) {
        throw unsupportedOperationException();
    }

    @Override
    public void writeByte(byte b) {
        throw unsupportedOperationException();
    }

    @Override
    public void setByte(int index, byte b) {
        throw unsupportedOperationException();
    }

    @Override
    protected void writeByte0(byte b) {
        throw unsupportedOperationException();
    }

    @Override
    public void writeBytes(byte[] src) {
        throw unsupportedOperationException();
    }

    @Override
    public int writeBytes(byte[] src, int offset, int length) {
        throw unsupportedOperationException();
    }

    @Override
    public int writeBytes(ByteBuf src) {
        throw unsupportedOperationException();
    }

    @Override
    public int writeBytes(ByteBuf src, int length) {
        throw unsupportedOperationException();
    }

    @Override
    public int writeBytes(ByteBuffer src) {
        throw unsupportedOperationException();
    }

    @Override
    public int writeBytes(ByteBuffer src, int length) {
        throw unsupportedOperationException();
    }

    @Override
    protected int writeBytes0(byte[] src, int offset, int length) {
        throw unsupportedOperationException();
    }

    @Override
    protected int writeBytes00(ByteBuf src, int len) {
        throw unsupportedOperationException();
    }

    @Override
    protected int writeBytes00(ByteBuffer src, int len) {
        throw unsupportedOperationException();
    }

    @Override
    public void writeInt(int value) {
        throw unsupportedOperationException();
    }

    @Override
    public void setInt(int index, int value) {
        throw unsupportedOperationException();
    }

    @Override
    protected void writeInt0(int value) {
        throw unsupportedOperationException();
    }

    @Override
    public void writeIntLE(int value) {
        throw unsupportedOperationException();
    }

    @Override
    public void setIntLE(int index, int value) {
        throw unsupportedOperationException();
    }

    @Override
    protected void writeIntLE0(int value) {
        throw unsupportedOperationException();
    }

    @Override
    public void setLong(int index, long value) {
        throw unsupportedOperationException();
    }

    @Override
    public float readFloat() {
        return m.getFloat();
    }

    @Override
    public float getFloat(int index) {
        return p.getFloat(index);
    }

    @Override
    public float readFloatLE() {
        return Float.intBitsToFloat(readIntLE());
    }

    @Override
    public float getFloatLE(int index) {
        return p.getFloatLE(index);
    }

    @Override
    public double readDouble() {
        return m.getDouble();
    }

    @Override
    public double getDouble(int index) {
        return p.getDouble(index);
    }

    @Override
    public double readDoubleLE() {
        return Double.longBitsToDouble(readLongLE());
    }

    @Override
    public double getDoubleLE(int index) {
        return p.getDoubleLE(index);
    }

    @Override
    public void setDouble(int index, double value) {
        throw unsupportedOperationException();
    }

    @Override
    public void setDoubleLE(int index, double value) {
        throw unsupportedOperationException();
    }

    @Override
    public void setFloat(int index, float value) {
        throw unsupportedOperationException();
    }

    @Override
    public void setFloatLE(int index, float value) {
        throw unsupportedOperationException();
    }

    @Override
    public void writeLong(long value) {
        throw unsupportedOperationException();
    }

    @Override
    protected void writeLong0(long value) {
        throw unsupportedOperationException();
    }

    @Override
    public void setLongLE(int index, long value) {
        throw unsupportedOperationException();
    }

    @Override
    public void writeLongLE(long value) {
        throw unsupportedOperationException();
    }

    @Override
    protected void writeLongLE0(long value) {
        throw unsupportedOperationException();
    }

    @Override
    public void writeShort(int value) {
        throw unsupportedOperationException();
    }

    @Override
    public void setShort(int index, int value) {
        throw unsupportedOperationException();
    }

    @Override
    protected void writeShort0(int value) {
        throw unsupportedOperationException();
    }

    @Override
    public void writeShortLE(int value) {
        throw unsupportedOperationException();
    }

    @Override
    public void setShortLE(int index, int value) {
        throw unsupportedOperationException();
    }

    @Override
    protected void writeShortLE0(int value) {
        throw unsupportedOperationException();
    }

    @Override
    public ByteBuf reverseRead() {
        abs_read_index = getNioBuffer().position();
        return this;
    }

    @Override
    public ByteBuf reverseWrite() {
        throw unsupportedOperationException();
    }

    @Override
    protected void release0() {
        p.release();
    }

    @Override
    public int readableBytes() {
        return m.remaining();
    }

    @Override
    public ByteBuf resetWriteIndex() {
        throw unsupportedOperationException();
    }

    @Override
    public ByteBuf resetReadIndex() {
        throw unsupportedOperationException();
    }

    @Override
    public ByteBuf skipRead(int length) {
        m.position(m.position() + length);
        return this;
    }


}
