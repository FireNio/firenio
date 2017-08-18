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
package com.generallycloud.baseio.common;

import java.nio.ByteBuffer;

public class ByteBufferUtil {

    @Deprecated
    public static void read(ByteBuffer dest, ByteBuffer src) {

        int srcRemaing = src.remaining();

        if (srcRemaing == 0) {
            return;
        }

        int remaining = dest.remaining();

        if (remaining == 0) {
            return;
        }

        if (remaining <= srcRemaing) {

            dest.put(src.array(), src.position(), remaining);

            src.position(src.position() + remaining);

        } else {

            dest.put(src.array(), src.position(), srcRemaing);

            src.position(src.limit());
        }
    }

    @SuppressWarnings("restriction")
    public static void release(ByteBuffer buffer) {
        if (((sun.nio.ch.DirectBuffer) buffer).cleaner() != null) {
            ((sun.nio.ch.DirectBuffer) buffer).cleaner().clean();
        }

    }

}
