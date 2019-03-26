/*
 * Copyright 2015 The Baseio Project
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
package com.firenio.baseio.buffer;

import java.nio.ByteBuffer;

/**
 * @author wangkai
 *
 */
final class DuplicatedUnsafeByteBuf extends UnsafeByteBuf {

    private ByteBuf    p;

    DuplicatedUnsafeByteBuf(ByteBuf proto, int refCnt) {
        super(proto.address());
        this.p = proto;
        this.referenceCount = refCnt;
        this.capacity(proto.capacity());
        this.absLimit(proto.absLimit());
        this.absPos(proto.absPos());
    }
    
    @Override
    public long address() {
        return p.address();
    }

    @Override
    public byte absByte(int pos) {
        return p.absByte(pos);
    }

    @Override
    public int absLimit() {
        return p.absLimit();
    }

    @Override
    public ByteBuf absLimit(int limit) {
        throw unsupportedOperationException();
    }
    
    @Override
    public boolean isPooled() {
        return p.isPooled();
    }

    @Override
    public int absPos() {
        return p.absPos();
    }

    @Override
    public ByteBuf absPos(int absPos) {
        throw unsupportedOperationException();
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
        throw unsupportedOperationException();
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
    public void getBytes(byte[] dst, int offset, int length) {
        p.getBytes(dst, offset, length);
    }

    @Override
    protected int get0(ByteBuffer dst, int len) {
        throw unsupportedOperationException();
    }

    @Override
    public byte getByte() {
        throw unsupportedOperationException();
    }

    @Override
    public byte getByte(int index) {
        return p.getByte(index);
    }

    @Override
    public int getInt() {
        throw unsupportedOperationException();
    }

    @Override
    public int getInt(int index) {
        return p.getInt(index);
    }

    @Override
    public int getIntLE() {
        throw unsupportedOperationException();
    }

    @Override
    public int getIntLE(int index) {
        return p.getIntLE(index);
    }

    @Override
    public long getLong() {
        throw unsupportedOperationException();
    }

    @Override
    public long getLong(int index) {
        return p.getLong(index);
    }

    @Override
    public long getLongLE() {
        throw unsupportedOperationException();
    }

    @Override
    public long getLongLE(int index) {
        return p.getLongLE(index);
    }

    @Override
    public short getShort() {
        throw unsupportedOperationException();
    }

    @Override
    public short getShort(int index) {
        return p.getShort(index);
    }

    @Override
    public short getShortLE() {
        throw unsupportedOperationException();
    }

    @Override
    public short getShortLE(int index) {
        return p.getShortLE(index);
    }

    @Override
    public short getUnsignedByte() {
        throw unsupportedOperationException();
    }

    @Override
    public short getUnsignedByte(int index) {
        return p.getUnsignedByte(index);
    }

    @Override
    public long getUnsignedInt() {
        throw unsupportedOperationException();
    }

    @Override
    public long getUnsignedInt(int index) {
        return p.getUnsignedInt(index);
    }

    @Override
    public long getUnsignedIntLE() {
        throw unsupportedOperationException();
    }

    @Override
    public long getUnsignedIntLE(int index) {
        return p.getUnsignedIntLE(index);
    }

    @Override
    public int getUnsignedShort() {
        throw unsupportedOperationException();
    }

    @Override
    public int getUnsignedShort(int index) {
        return p.getUnsignedShort(index);
    }

    @Override
    public int getUnsignedShortLE() {
        throw unsupportedOperationException();
    }

    @Override
    public int getUnsignedShortLE(int index) {
        return p.getUnsignedShortLE(index);
    }

    @Override
    public boolean hasArray() {
        return p.hasArray();
    }

    @Override
    public int indexOf(byte b, int absPos, int size) {
        return p.indexOf(b, absPos, size);
    }

    @Override
    public int lastIndexOf(byte b, int absPos, int size) {
        return p.lastIndexOf(b, absPos, size);
    }

    @Override
    public ByteBuf limit(int limit) {
        throw unsupportedOperationException();
    }

    @Override
    public ByteBuf markL() {
        throw unsupportedOperationException();
    }

    @Override
    public ByteBuf markP() {
        throw unsupportedOperationException();
    }

    @Override
    public ByteBuf position(int position) {
        throw unsupportedOperationException();
    }

    @Override
    public void putBytes(byte[] src) {
        throw unsupportedOperationException();
    }

    @Override
    public void putBytes(byte[] src, int offset, int length) {
        throw unsupportedOperationException();
    }

    @Override
    public int putBytes(ByteBuf src) {
        throw unsupportedOperationException();
    }

    @Override
    public int putBytes(ByteBuf src, int length) {
        throw unsupportedOperationException();
    }

    @Override
    public int putBytes(ByteBuffer src) {
        throw unsupportedOperationException();
    }

    @Override
    public int putBytes(ByteBuffer src, int length) {
        throw unsupportedOperationException();
    }

    @Override
    protected void putBytes0(byte[] src, int offset, int length) {
        throw unsupportedOperationException();
    }

    @Override
    protected int putBytes00(ByteBuf src, int len) {
        throw unsupportedOperationException();
    }

    @Override
    protected int putBytes00(ByteBuffer src, int len) {
        throw unsupportedOperationException();
    }

    @Override
    public void putByte(byte b) {
        throw unsupportedOperationException();
    }

    @Override
    public void putByte(int index, byte b) {
        throw unsupportedOperationException();
    }

    @Override
    protected void putByte0(byte b) {
        throw unsupportedOperationException();
    }

    @Override
    public void putInt(int value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putInt(int index, int value) {
        throw unsupportedOperationException();
    }

    @Override
    protected void putInt0(int value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putIntLE(int value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putIntLE(int index, int value) {
        throw unsupportedOperationException();
    }

    @Override
    protected void putIntLE0(int value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putLong(int index, long value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putLong(long value) {
        throw unsupportedOperationException();
    }

    @Override
    protected void putLong0(long value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putLongLE(int index, long value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putLongLE(long value) {
        throw unsupportedOperationException();
    }

    @Override
    protected void putLongLE0(long value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putShort(int index, short value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putShort(short value) {
        throw unsupportedOperationException();
    }

    @Override
    protected void putShort0(short value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putShortLE(int index, short value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putShortLE(short value) {
        throw unsupportedOperationException();
    }

    @Override
    protected void putShortLE0(short value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putUnsignedInt(int index, long value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putUnsignedInt(long value) {
        throw unsupportedOperationException();
    }

    @Override
    protected void putUnsignedInt0(long value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putUnsignedIntLE(int index, long value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putUnsignedIntLE(long value) {
        throw unsupportedOperationException();
    }

    @Override
    protected void putUnsignedIntLE0(long value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putUnsignedShort(int value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putUnsignedShort(int index, int value) {
        throw unsupportedOperationException();
    }

    @Override
    protected void putUnsignedShort0(int value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putUnsignedShortLE(int value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putUnsignedShortLE(int index, int value) {
        throw unsupportedOperationException();
    }

    @Override
    protected void putUnsignedShortLE0(int value) {
        throw unsupportedOperationException();
    }

    @Override
    protected void release0() {
        p.release();
    }

    @Override
    public ByteBuf resetL() {
        throw unsupportedOperationException();
    }

    @Override
    public ByteBuf resetP() {
        throw unsupportedOperationException();
    }

    @Override
    public ByteBuf reverse() {
        return this;
    }

    private UnsupportedOperationException unsupportedOperationException() {
        return new UnsupportedOperationException();
    }

}
