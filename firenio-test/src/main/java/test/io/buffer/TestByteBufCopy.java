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
package test.io.buffer;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import com.firenio.buffer.ByteBuf;
import com.firenio.common.Util;

import junit.framework.Assert;

/**
 * @author wangkai
 */
public class TestByteBufCopy {

    static final byte[]     _data = "abc123abc123".getBytes();
    static final ByteBuf    arrayData;
    static final ByteBuf    directData;
    static final ByteBuffer jArrayData;
    static final ByteBuffer jDirectData;

    static {
        jArrayData = ByteBuffer.wrap(_data);
        jDirectData = ByteBuffer.allocateDirect(12);
        jArrayData.position(6);
        jDirectData.put(_data).position(6);
        arrayData = ByteBuf.wrap(jArrayData).skip(6);
        directData = ByteBuf.wrap(jDirectData).skip(6);
    }

    static ByteBuf _array() {
        return ByteBuf.heap(12).position(6);
    }

    static ByteBuf _direct() {
        return ByteBuf.direct(12).position(6);
    }

    static ByteBuffer _jArray() {
        return (ByteBuffer) ByteBuffer.allocate(12).position(6);
    }

    static ByteBuffer _jDirect() {
        return (ByteBuffer) ByteBuffer.allocateDirect(12).position(6);
    }

    static ByteBuf arrayData() {
        return arrayData.limit(12).position(6);
    }

    static ByteBuf directData() {
        return directData.limit(12).position(6);
    }

    static ByteBuffer jArrayData() {
        jArrayData.limit(12).position(6);
        return jArrayData;
    }

    static ByteBuffer jDirectData() {
        jDirectData.limit(12).position(6);
        return jDirectData;
    }

    void _invoke(Method m) throws Exception {
        String name = m.getName();
        if (name == null) {
            System.out.println();
        }
        Object res = m.invoke(this, (Object[]) null);
        byte[] bytes;
        if (res instanceof ByteBuf) {
            ((ByteBuf) res).flip();
            ((ByteBuf) res).position(((ByteBuf) res).limit() - 6);
            bytes = ((ByteBuf) res).getBytes();
        } else if (res instanceof ByteBuffer) {
            ((ByteBuffer) res).flip();
            ((ByteBuffer) res).position(((ByteBuffer) res).limit() - 6);
            bytes = new byte[6];
            ((ByteBuffer) res).get(bytes);
        } else {
            bytes = "1".getBytes();
        }
        Assert.assertEquals("abc123", new String(bytes));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void _testAll() throws Exception {

        Method[]     ms   = TestByteBufCopy.class.getDeclaredMethods();
        List<Method> list = Util.array2List(ms);
        list.sort(Comparator.comparing(Method::getName));

        for (int i = 0; i < list.size(); i++) {
            Method m    = list.get(i);
            String name = m.getName();
            if ("main".equals(name) || name.startsWith("_")) {
                continue;
            }
            System.out.println("------------------" + name + "-----------------");
            m.setAccessible(true);
            _invoke(m);
        }
    }

    Object arrayGetArray() {
        ByteBuf buf = _array();
        arrayData().getBytes(buf);
        return buf;
    }

    Object arrayGetDirect() {
        ByteBuf buf = _direct();
        arrayData().getBytes(buf);
        return buf;
    }

    Object arrayGetJArray() {
        ByteBuffer buf = _jArray();
        arrayData().getBytes(buf);
        return buf;
    }

    Object arrayGetJDirect() {
        ByteBuffer buf = _jDirect();
        arrayData().getBytes(buf);
        return buf;
    }

    Object arrayPutArray() {
        ByteBuf buf = _array();
        buf.putBytes(arrayData());
        return buf;
    }

    Object arrayPutDirect() {
        ByteBuf buf = _array();
        buf.putBytes(directData());
        return buf;
    }

    Object arrayPutJArray() {
        ByteBuf buf = _array();
        buf.putBytes(jArrayData());
        return buf;
    }

    Object arrayPutJDirect() {
        ByteBuf buf = _array();
        buf.putBytes(jDirectData());
        return buf;
    }

    Object directGetArray() {
        ByteBuf buf = _array();
        directData().getBytes(buf);
        return buf;
    }

    Object directGetDirect() {
        ByteBuf buf = _direct();
        directData().getBytes(buf);
        return buf;
    }

    Object directGetJArray() {
        ByteBuffer buf = _jArray();
        directData().getBytes(buf);
        return buf;
    }

    Object directGetJDirect() {
        ByteBuffer buf = _jDirect();
        directData().getBytes(buf);
        return buf;
    }

    Object directPutArray() {
        ByteBuf buf = _direct();
        buf.putBytes(arrayData());
        return buf;
    }

    Object directPutDirect() {
        ByteBuf buf = _direct();
        buf.putBytes(directData());
        return buf;
    }

    Object directPutJArray() {
        ByteBuf buf = _direct();
        buf.putBytes(jArrayData());
        return buf;
    }

    Object directPutJDirect() {
        ByteBuf buf = _direct();
        buf.putBytes(jDirectData());
        return buf;
    }

}
