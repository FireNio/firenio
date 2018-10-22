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
package com.generallycloud.baseio.component;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.collection.Attributes;
import com.generallycloud.baseio.collection.AttributesImpl;
import com.generallycloud.baseio.common.ReleaseUtil;

/**
 * @author wangkai
 *
 */
public final class FastThreadLocal extends AttributesImpl implements Attributes {

    private static final AtomicInteger                indexedVarsIndex   = new AtomicInteger(0);
    private static final int                          maxIndexedVarsSize = 16;
    private static final ThreadLocal<FastThreadLocal> slowThreadLocal    = new ThreadLocal<>();

    private Map<Charset, CharsetDecoder>              charsetDecoders    = new IdentityHashMap<>();
    private Map<Charset, CharsetEncoder>              charsetEncoders    = new IdentityHashMap<>();
    private Object[]                                  indexedVariables   = new Object[maxIndexedVarsSize];
    private ByteBuf                                   sslWrapBuf;
    private ByteBuf                                   sslUnwrapBuf;

    FastThreadLocal() {}

    private void destroy0() {
        ReleaseUtil.release(sslWrapBuf);
        ReleaseUtil.release(sslUnwrapBuf);
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

    public Object getIndexedVariable(int index) {
        return indexedVariables[index];
    }

    public ByteBuf getSslWrapBuf() {
        if (sslWrapBuf == null) {
            ByteBufAllocator allocator = UnpooledByteBufAllocator.getDirect();
            sslWrapBuf = allocator.allocate(SslContext.SSL_PACKET_BUFFER_SIZE);
        }
        return sslWrapBuf;
    }
    
    public ByteBuf getSslUnwrapBuf() {
        if (sslUnwrapBuf == null) {
            ByteBufAllocator allocator = UnpooledByteBufAllocator.getDirect();
            sslUnwrapBuf = allocator.allocate(SslContext.SSL_UNWRAP_BUFFER_SIZE);
        }
        return sslUnwrapBuf;
    }

    public void setIndexedVariable(int index, Object value) {
        indexedVariables[index] = value;
    }

    public static void destroy() {
        Thread thread = Thread.currentThread();
        FastThreadLocal l;
        if (thread instanceof FastThreadLocalThread) {
            l = ((FastThreadLocalThread) thread).getThreadLocal();
        } else {
            l = slowThreadLocal.get();
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
            FastThreadLocal l = slowThreadLocal.get();
            if (l == null) {
                l = new FastThreadLocal();
                slowThreadLocal.set(l);
            }
            return l;
        }
    }

    public static int nextIndexedVariablesIndex() {
        if (indexedVarsIndex.get() >= maxIndexedVarsSize) {
            return -1;
        }
        int index = indexedVarsIndex.getAndIncrement();
        if (index >= maxIndexedVarsSize) {
            return -1;
        }
        return index;
    }

}
