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
package test.io;

import java.io.File;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

import com.firenio.common.Unsafe;

/**
 * @author: wangkai
 **/
public class TestMMap {

    static OpenOption[] FC_OPS = new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING};

    public static void main(String[] args) throws Exception {
        File             file1 = new File("C://temp/test_mmap1.txt");
        File             file2 = new File("C://temp/test_mmap2.txt");
        FileChannel      ch1   = FileChannel.open(file1.toPath(), FC_OPS);
        FileChannel      ch2   = FileChannel.open(file2.toPath(), FC_OPS);
        MappedByteBuffer buf1  = ch1.map(MapMode.READ_WRITE, 0, 32);
        MappedByteBuffer buf2  = ch2.map(MapMode.READ_WRITE, 0, 32);
        byte[]           data  = "abc123456".getBytes();
        buf1.put(data);
        buf1.put((byte) 1);
        buf1.put((byte) 2);
        buf1.put((byte) 3);
        buf1.put((byte) 4);
        Unsafe.copyFromArray(data, 0, Unsafe.address(buf2), data.length);
        Unsafe.copyFromArray(data, 0, Unsafe.address(buf1) + 15, data.length);
        Unsafe.copyFromArray(data, 0, Unsafe.address(buf2) + 15, data.length);
        System.out.println("finish...");
        System.exit(0);
    }


}
