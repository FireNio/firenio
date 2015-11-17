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
package test.io.buffer;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import com.firenio.baseio.buffer.ByteBuf;
import com.firenio.baseio.common.Util;

import junit.framework.Assert;

/**
 * @author wangkai
 *
 */
public class TestByteBufCopy {

    static final byte[]     _data = "abc123abc123".getBytes();
    static final ByteBuffer jArrayData;
    static final ByteBuffer jDirectData;
    static final ByteBuf    arrayData;
    static final ByteBuf    directData;

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

    static ByteBuffer jDirectData() {
        jDirectData.limit(12).position(6);
        return jDirectData;
    }

    static ByteBuffer jArrayData() {
        jArrayData.limit(12).position(6);
        return jArrayData;
    }

    Object arrayGetArray() {
        ByteBuf buf = _array();
        arrayData().get(buf);
        return buf;
    }

    Object arrayGetDirect() {
        ByteBuf buf = _direct();
        arrayData().get(buf);
        return buf;
    }

    Object arrayGetJArray() {
        ByteBuffer buf = _jArray();
        arrayData().get(buf);
        return buf;
    }

    Object arrayGetJDirect() {
        ByteBuffer buf = _jDirect();
        arrayData().get(buf);
        return buf;
    }

    Object arrayPutArray() {
        ByteBuf buf = _array();
        buf.put(arrayData());
        return buf;
    }

    Object arrayPutDirect() {
        ByteBuf buf = _array();
        buf.put(directData());
        return buf;
    }

    Object arrayPutJArray() {
        ByteBuf buf = _array();
        buf.put(jArrayData());
        return buf;
    }

    Object arrayPutJDirect() {
        ByteBuf buf = _array();
        buf.put(jDirectData());
        return buf;
    }

    Object directGetArray() {
        ByteBuf buf = _array();
        directData().get(buf);
        return buf;
    }

    Object directGetDirect() {
        ByteBuf buf = _direct();
        directData().get(buf);
        return buf;
    }

    Object directGetJArray() {
        ByteBuffer buf = _jArray();
        directData().get(buf);
        return buf;
    }

    Object directGetJDirect() {
        ByteBuffer buf = _jDirect();
        directData().get(buf);
        return buf;
    }

    Object directPutArray() {
        ByteBuf buf = _direct();
        buf.put(arrayData());
        return buf;
    }

    Object directPutDirect() {
        ByteBuf buf = _direct();
        buf.put(directData());
        return buf;
    }

    Object directPutJArray() {
        ByteBuf buf = _direct();
        buf.put(jArrayData());
        return buf;
    }

    Object directPutJDirect() {
        ByteBuf buf = _direct();
        buf.put(jDirectData());
        return buf;
    }

    void _invoke(Method m) throws Exception {
        String name = m.getName();
        if (name == null) {

        }
        Object res = m.invoke(this, null);
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
        Assert.assertTrue("abc123".equals(new String(bytes)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void _testAll() throws Exception {

        Method[] ms = TestByteBufCopy.class.getDeclaredMethods();
        List<Method> list = Util.array2List(ms);
        Collections.sort(list, new Comparator<Method>() {
            public int compare(Method o1, Method o2) {
                return o1.getName().compareTo(o2.getName());
            };
        });

        for (int i = 0; i < list.size(); i++) {
            Method m = list.get(i);
            String name = m.getName();
            if ("main".equals(name) || name.startsWith("_")) {
                continue;
            }
            System.out.println("------------------" + name + "-----------------");
            m.setAccessible(true);
            _invoke(m);
        }
    }

}
