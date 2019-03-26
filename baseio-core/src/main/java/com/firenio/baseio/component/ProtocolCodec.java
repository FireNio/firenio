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

import java.io.IOException;

import com.firenio.baseio.buffer.ByteBuf;
import com.firenio.baseio.common.Util;

/**
 * @author wangkai
 *
 */
public abstract class ProtocolCodec {

    // 可能会遭受一种攻击，比如最大可接收数据为100，客户端传输到99后暂停，
    // 这样多次以后可能会导致内存溢出
    public abstract Frame decode(Channel ch, ByteBuf src) throws Exception;

    // 注意：encode失败要release掉encode过程中申请的内存
    public abstract ByteBuf encode(Channel ch, Frame frame) throws Exception;

    public abstract String getProtocolId();

    public abstract int getHeaderLength();

    public Frame ping(Channel ch) {
        return null;
    }

    public Frame pong(Channel ch, Frame ping) {
        return ping.setPong();
    }

    protected Object newAttachment() {
        return null;
    }

    public void release(NioEventLoop eventLoop, Frame frame) {}

    protected static IOException EXCEPTION(Class<?> clazz, String method, String msg) {
        return Util.unknownStackTrace(new IOException(msg), clazz, method);
    }

    protected static IOException EXCEPTION(String msg) {
        return EXCEPTION("decode", msg);
    }

    protected static IOException EXCEPTION(String method, String msg) {
        Class<?> clazz = null;
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts.length > 1) {
            String thisClassName = ProtocolCodec.class.getName();
            String className = null;
            for (int i = 1; i < sts.length; i++) {
                String name = sts[i].getClassName();
                if (!name.equals(thisClassName)) {
                    className = name;
                    break;
                }
            }
            if (className != null) {
                try {
                    clazz = Class.forName(sts[3].getClassName());
                } catch (ClassNotFoundException e) {}
            }
        }
        if (clazz == null) {
            clazz = ProtocolCodec.class;
        }
        return EXCEPTION(clazz, method, msg);
    }

    protected static int nextIndexedVariablesIndex() {
        return FastThreadLocal.nextIndexedVariablesIndex();
    }

}
