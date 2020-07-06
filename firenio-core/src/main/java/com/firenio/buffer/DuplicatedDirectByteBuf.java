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
final class DuplicatedDirectByteBuf extends DirectByteBuf {

    private ByteBuf p;

    DuplicatedDirectByteBuf(ByteBuf proto, int refCnt) {
        super(proto.getNioBuffer().duplicate());
        this.p = proto;
        this.offset(proto.offset());
        this.absWriteIndex(proto.absWriteIndex());
        this.absReadIndex(proto.absReadIndex());
        this.referenceCount = refCnt;
    }

    @Override
    public byte getByteAbs(int pos) {
        return p.getByteAbs(pos);
    }

    @Override
    public int capacity() {
        return p.capacity();
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
    public boolean isPooled() {
        return p.isPooled();
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
    public int setBytes(int index, ByteBuf src) {
        throw unsupportedOperationException();
    }

    @Override
    public int setBytes(int index, ByteBuffer src) {
        throw unsupportedOperationException();
    }

    @Override
    public int setBytes(int index, ByteBuf src, int length) {
        throw unsupportedOperationException();
    }

    @Override
    public int setBytes(int index, ByteBuffer src, int length) {
        throw unsupportedOperationException();
    }

    @Override
    public int setBytes(int index, byte[] src, int offset, int length) {
        throw unsupportedOperationException();
    }

    @Override
    public void setBytes(int index, byte[] src) {
        throw unsupportedOperationException();
    }

    @Override
    protected int setBytes0(int index, ByteBuf src, int len) {
        throw unsupportedOperationException();
    }

    @Override
    protected int setBytes0(int index, ByteBuffer src, int len) {
        throw unsupportedOperationException();
    }

    @Override
    protected int setBytes0(int index, byte[] src, int offset, int length) {
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
    protected int writeBytes0(ByteBuf src, int len) {
        throw unsupportedOperationException();
    }

    @Override
    public int setBytes0(int index, long address, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int writeBytes0(long address, int length) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int writeBytes0(ByteBuffer src, int len) {
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
    public ByteBuf reverseWrite() {
        throw unsupportedOperationException();
    }

    @Override
    protected void release0() {
        p.release();
    }

}
