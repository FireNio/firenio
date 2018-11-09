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
package com.generallycloud.baseio.buffer;

import java.nio.ByteBuffer;

public class ByteBufUtil {

    @SuppressWarnings("restriction")
    public static void release(ByteBuffer buffer) {
        if (((sun.nio.ch.DirectBuffer) buffer).cleaner() != null) {
            ((sun.nio.ch.DirectBuffer) buffer).cleaner().clean();
        }
    }

    public static ByteBuf wrap(byte[] data) {
        return UnpooledByteBufAllocator.getHeap().wrap(data);
    }

    public static ByteBuf wrap(byte[] data, int offset, int length) {
        return UnpooledByteBufAllocator.getHeap().wrap(data, offset, length);
    }

    public static ByteBuf wrap(ByteBuffer buffer) {
        return UnpooledByteBufAllocator.getHeap().wrap(buffer);
    }

}
