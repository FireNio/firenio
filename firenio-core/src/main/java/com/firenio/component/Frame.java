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
package com.firenio.component;

import java.nio.charset.Charset;

import com.firenio.buffer.ByteBuf;
import com.firenio.common.Util;

public abstract class Frame {

    private Object content;

    public byte[] getArrayContent() {
        return (byte[]) content;
    }

    public ByteBuf getBufContent() {
        return (ByteBuf) content;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public String getFrameName() {
        return null;
    }

    public String getStringContent() {
        return (String) content;
    }

    //is last or continue
    public boolean isLast() {
        return true;
    }

    //is text or binary
    public boolean isText() {
        return true;
    }

    public void release() {
        Util.release(content);
    }

    public Frame reset() {
        this.content = null;
        return this;
    }

    public void setBytes(Channel ch, byte[] bytes) {
        setBytes(ch, bytes, 0, bytes.length);
    }

    public void setBytes(int header, byte[] bytes) {
        setBytes(header, bytes, 0, bytes.length);
    }

    public void setBytes(Channel ch, byte[] bytes, int off, int len) {
        int h = ch.getCodec().getHeaderLength();
        setBytes(h, bytes, off, len);
    }

    public void setBytes(int header, byte[] bytes, int off, int len) {
        this.content = ByteBuf.heap(header + len).skip(header);
        write(bytes, off, len);
    }

    public void setString(Channel ch, String value) {
        setBytes(ch, value.getBytes(ch.getCharset()));
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
