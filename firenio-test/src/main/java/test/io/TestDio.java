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

import com.firenio.DevelopConfig;
import com.firenio.common.Unsafe;
import com.firenio.component.Native;
import com.firenio.log.LoggerFactory;

/**
 * @program: firenio
 * @description:
 * @author: wangkai
 * @create: 2019-09-11 16:18
 **/
public class TestDio {

    static {
        LoggerFactory.setEnableSLF4JLogger(false);
        DevelopConfig.EPOLL_DEBUG = true;
        DevelopConfig.NATIVE_DEBUG = true;
    }

    static final int PS = 1024 * 4;

    static final String path = "/home/test/temp/test_direct2.txt";

    public static void main(String[] args) throws Exception {

        test_direct_write();
        test_direct_read();
    }

    static void test_direct_write() {
        long address = Native.posix_memalign_allocate(PS, PS);
        int  fd      = Native.open(path, Native.O_WRONLY | Native.O_CREAT | Native.O_DIRECT | Native.O_TRUNC, 0755);
        System.out.println("file len: " + Native.file_length(fd));
        byte[] data = "hello world!0".getBytes();
        do_write_data(fd, address, data, 0);
        do_write_data(fd, address, data, 1);
        do_write_data(fd, address, data, 2);
        do_write_data(fd, address, data, 3);
        System.out.println("file len: " + Native.file_length(fd));
        Unsafe.free(address);
        Native.close(fd);
    }

    static void do_write_data(int fd, long address, byte[] data, int index) {
        data[data.length - 1] = (byte) (index);
        Unsafe.copyFromArray(data, 0, address, data.length);
        if (index > 0) {
            Native.pwrite(fd, address, PS, index * PS);
        } else {
            Native.write(fd, address, PS);
        }

    }

    static void test_direct_read() {
        long   address  = Native.posix_memalign_allocate(PS, PS);
        int    fd       = Native.open(path, Native.O_RDONLY | Native.O_CREAT | Native.O_DIRECT, 0755);
        long   file_len = Native.file_length(fd);
        byte[] data     = new byte[16];
        System.out.println("file len: " + file_len);
        Native.lseek(fd, file_len, Native.SEEK_SET);
        do_read_data(fd, address, data, 0);
        do_read_data(fd, address, data, PS * 2);
        Unsafe.free(address);
        Native.close(fd);
    }

    static void do_read_data(int fd, long address, byte[] data, long pos) {
        int read;
        if (pos == 0) {
            Native.lseek(fd, pos, Native.SEEK_SET);
            read = Native.read(fd, address, PS);
        } else {
            read = Native.pread(fd, address, PS, pos);
        }
        System.out.println("read len: " + read);
        Unsafe.copyToArray(address, data, 0, 16);
        String content = new String(data);
        System.out.println("content: " + content);

    }

}
