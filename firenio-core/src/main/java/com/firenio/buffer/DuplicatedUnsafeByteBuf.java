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
final class DuplicatedUnsafeByteBuf extends UnsafeByteBuf {

    private ByteBuf p;

    DuplicatedUnsafeByteBuf(ByteBuf proto, int refCnt) {
        super(proto.address());
        this.p = proto;
        this.referenceCount = refCnt;
        this.capacity(proto.capacity());
        this.absLimit(proto.absLimit());
        this.absPos(proto.absPos());
    }

    @Override
    public ByteBuf absLimit(int limit) {
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
    public void putDouble(int index, double value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putDoubleLE(int index, double value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putFloat(int index, float value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putFloatLE(int index, float value) {
        throw unsupportedOperationException();
    }

    @Override
    public boolean isPooled() {
        return p.isPooled();
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
    public void putBytes(byte[] src) {
        throw unsupportedOperationException();
    }

    @Override
    public int putBytes(byte[] src, int offset, int length) {
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
    protected int putBytes0(byte[] src, int offset, int length) {
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
    public void putShort(int value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putShort(int index, int value) {
        throw unsupportedOperationException();
    }

    @Override
    protected void putShort0(int value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putShortLE(int value) {
        throw unsupportedOperationException();
    }

    @Override
    public void putShortLE(int index, int value) {
        throw unsupportedOperationException();
    }

    @Override
    protected void putShortLE0(int value) {
        throw unsupportedOperationException();
    }

    @Override
    protected void release0() {
        p.release();
    }

}
