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

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import com.firenio.buffer.ByteBuf;
import com.firenio.buffer.ByteBufListener;
import com.firenio.common.Util;
import com.firenio.component.Channel;

/**
 * @author: wangkai
 **/
public class Test7 {

    public static void main(String[] args) throws Exception {
        ByteBuf buf = ByteBuf.buffer(1024);
        buf.setListener(new ByteBufListener() {
            @Override
            public void onComplete(Channel channel) {

            }
        });

        ByteBuf duplicate = buf.duplicate();
        System.out.println(buf.getListener());
        System.out.println(duplicate.getListener());


    }

    static void printInputStream(String clazz) {
        try {
            String      name  = clazz.replace(".", "/") + ".class";
            InputStream is    = Test7.class.getClassLoader().getResourceAsStream(name);
            int         len   = is.available();
            byte[]      bb    = new byte[len];
            int         r_len = 0;
            for (; r_len != len; ) {
                r_len += is.read(bb, r_len, len - r_len);
            }
            System.out.println(clazz + ">>" + Base64.getEncoder().encodeToString(bb));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
