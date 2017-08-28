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
package com.generallycloud.test.others;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.ByteArrayBuffer;

public class TempTest {

    private static ByteArrayBuffer writeBinaryBuffer;

    private static long[]          ls  = new long[10];
    private static Long[]          ls2 = new Long[10];

    public static void main(String[] args) throws ClassNotFoundException, Exception {

        List<String> ls = FileUtil.readLines(FileUtil.readFileByCls("test2.txt"));

        FileOutputStream out = new FileOutputStream(new File("test3.Queue"), false);

        for (String s : ls) {
            if (StringUtil.isNullOrBlank(s)) {
                continue;
            }
            String[] ss = s.split(",");
            for (String bs : ss) {
                if (StringUtil.isNullOrBlank(bs)) {
                    continue;
                }
                out.write(Byte.parseByte(bs));
            }
        }

        out.close();
    }

    public static void writeBinary(byte[] bytes, int offset, int length) {

        if (writeBinaryBuffer == null) {
            writeBinaryBuffer = new ByteArrayBuffer();
        }

        writeBinaryBuffer.write(bytes, offset, length);
    }

}
