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
package com.firenio.baseio.component;

import java.nio.charset.Charset;

import com.firenio.baseio.buffer.ByteBuf;
import com.firenio.baseio.common.Util;

public abstract class Frame {

    private static final byte TYPE_PING   = -1;
    private static final byte TYPE_PONG   = -2;
    private static final byte TYPE_SILENT = -3;

    private Object            content;
    private byte              type;

    public byte[] getArrayContent() {
        return (byte[]) content;
    }

    public ByteBuf getBufContent() {
        return (ByteBuf) content;
    }

    public Object getContent() {
        return content;
    }

    public String getFrameName() {
        return null;
    }

    public String getStringContent() {
        return (String) content;
    }

    public abstract int headerLength();

    //is last or continue
    public boolean isLast() {
        return true;
    }

    public boolean isPing() {
        return type == TYPE_PING;
    }

    public boolean isPong() {
        return type == TYPE_PONG;
    }

    public boolean isSilent() {
        return type == TYPE_SILENT;
    }

    //is text or binary
    public boolean isText() {
        return true;
    }

    public boolean isTyped() {
        return type != 0;
    }

    public void release() {
        Util.release(content);
    }

    public Frame reset() {
        this.type = 0;
        this.content = null;
        return this;
    }

    public void setBytes(byte[] bytes) {
        setBytes(bytes, 0, bytes.length);
    }

    public void setBytes(byte[] bytes, int off, int len) {
        Util.release(this.content);
        int h = headerLength();
        this.content = ByteBuf.heap(h + len).skip(h);
        write(bytes, off, len);
    }

    public void setContent(Object content) {
        Util.release(this.content);
        this.content = content;
    }

    public Frame setPing() {
        this.type = TYPE_PING;
        return this;
    }

    public Frame setPong() {
        this.type = TYPE_PONG;
        return this;
    }

    public Frame setSilent() {
        this.type = TYPE_SILENT;
        return this;
    }

    public void setString(String value) {
        setBytes(value.getBytes());
    }

    public void setString(String text, Channel ch) {
        setString(text, ch.getCharset());
    }

    public void setString(String text, ChannelContext context) {
        setString(text, context.getCharset());
    }

    public void setString(String text, Charset charset) {
        setBytes(text.getBytes(charset));
    }

    public void write(byte[] bytes) {
        write(bytes, 0, bytes.length);
    }

    public void write(byte[] bytes, int off, int len) {
        ByteBuf c = getBufContent();
        if (c == null) {
            throw new NullPointerException("do setContent(buf) before write");
        }
        c.putBytes(bytes, off, len);
    }

    public void write(String text, Channel ch) {
        write(text, ch.getCharset());
    }

    public void write(String text, ChannelContext context) {
        write(text, context.getCharset());
    }

    public void write(String text, Charset charset) {
        write(text.getBytes(charset));
    }
    
    @Override
    public String toString() {
        if (isText()) {
            return getStringContent();
        }
        return super.toString();
    }

}
