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
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.firenio.buffer.ByteBuf;
import com.firenio.common.Util;

/**
 * @author wangkai
 */
public final class FastThreadLocal {

    static final AtomicInteger                ATTRIBUTE_KEYS    = new AtomicInteger();
    static final ThreadLocal<FastThreadLocal> SLOW_THREAD_LOCAL = new ThreadLocal<>();

    private ByteBuf                      sslWrapBuf;
    private ByteBuf                      sslUnwrapBuf;
    private byte[]                       bytes32         = new byte[32];
    private StringBuilder                stringBuilder   = new StringBuilder(512);
    private List<?>                      list            = new ArrayList<>();
    private Map<?, ?>                    map             = new HashMap<>();
    private Object[]                     attributes      = new Object[16];
    private Map<Charset, CharsetDecoder> charsetDecoders = new IdentityHashMap<>();
    private Map<Charset, CharsetEncoder> charsetEncoders = new IdentityHashMap<>();

    FastThreadLocal() {}

    public static void destroy() {
        Thread          thread = Thread.currentThread();
        FastThreadLocal l;
        if (thread instanceof FastThreadLocalThread) {
            l = ((FastThreadLocalThread) thread).getThreadLocal();
        } else {
            l = SLOW_THREAD_LOCAL.get();
        }
        if (l != null) {
            l.destroy0();
        }
    }

    public static FastThreadLocal get() {
        Thread thread = Thread.currentThread();
        if (thread instanceof FastThreadLocalThread) {
            return ((FastThreadLocalThread) thread).getThreadLocal();
        } else {
            FastThreadLocal l = SLOW_THREAD_LOCAL.get();
            if (l == null) {
                l = new FastThreadLocal();
                SLOW_THREAD_LOCAL.set(l);
            }
            return l;
        }
    }

    public static int nextAttributeKey() {
        return ATTRIBUTE_KEYS.getAndIncrement();
    }

    private void destroy0() {
        Util.release(sslWrapBuf);
        Util.release(sslUnwrapBuf);
    }

    public byte[] getBytes32() {
        return bytes32;
    }

    public CharsetDecoder getCharsetDecoder(Charset charset) {
        CharsetDecoder decoder = charsetDecoders.get(charset);
        if (decoder == null) {
            decoder = charset.newDecoder();
            charsetDecoders.put(charset, decoder);
        }
        return decoder;
    }

    public CharsetEncoder getCharsetEncoder(Charset charset) {
        CharsetEncoder encoder = charsetEncoders.get(charset);
        if (encoder == null) {
            encoder = charset.newEncoder();
            charsetEncoders.put(charset, encoder);
        }
        return encoder;
    }

    public ByteBuf getSslUnwrapBuf() {
        if (sslUnwrapBuf == null) {
            sslUnwrapBuf = ByteBuf.buffer(SslContext.SSL_UNWRAP_BUFFER_SIZE);
        }
        return sslUnwrapBuf;
    }

    public ByteBuf getSslWrapBuf() {
        if (sslWrapBuf == null) {
            sslWrapBuf = ByteBuf.buffer(SslContext.SSL_PACKET_BUFFER_SIZE);
        }
        return sslWrapBuf;
    }

    public StringBuilder getStringBuilder() {
        stringBuilder.setLength(0);
        return stringBuilder;
    }

    public List<?> getList() {
        list.clear();
        return list;
    }

    public Map<?, ?> getMap() {
        map.clear();
        return map;
    }

    public <T> T getAttribute(int index) {
        Object[] attributes = this.attributes;
        if (index < attributes.length) {
            return (T) attributes[index];
        }
        return null;
    }

    public void setAttribute(int index, Object value) {
        Object[] attributes = this.attributes;
        if (index < attributes.length) {
            attributes[index] = value;
        } else {
            this.attributes = Arrays.copyOf(attributes, index + 1);
            this.attributes[index] = value;
        }
    }

}
