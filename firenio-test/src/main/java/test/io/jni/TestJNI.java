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
package test.io.jni;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.firenio.common.FileUtil;
import com.firenio.common.Unsafe;

/**
 * @author wangkai
 */
public class TestJNI {

    static {
        try {
            //            loadNative("main.o");
            System.load("/home/test/git-rep/socket-epoll/debug/main2.o");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static native int add(int a, int b);

    public static native int get(byte[] array, int pos);

    public static native int getDirect(ByteBuffer buf, int pos);

    static void loadNative(String name) throws IOException {
        InputStream in      = TestJNI.class.getClassLoader().getResourceAsStream(name);
        File        tmpFile = File.createTempFile(name, ".o");
        byte[]      data    = FileUtil.inputStream2ByteArray(in);
        FileUtil.writeByFile(tmpFile, data);
        System.load(tmpFile.getAbsolutePath());
        tmpFile.deleteOnExit();
    }

    public static void main(String[] args) {
        test();

    }

    public static native int print(String v);

    public static native int put(byte[] array, int v, int pos);

    public static native int putDirect(ByteBuffer buf, int v, int pos);

    static void test() {

        print("hello world!");

        System.out.println(add(100, 200));

        byte[] a   = new byte[16];
        int    res = put(a, 100, 8);
        System.out.println(res);
        res = get(a, 8);
        System.out.println(res);

        ByteBuffer d = Unsafe.allocateDirectByteBuffer(16);
        res = putDirect(d, 100, 8);
        System.out.println(res);
        res = getDirect(d, 8);
        System.out.println(res);

    }

}
