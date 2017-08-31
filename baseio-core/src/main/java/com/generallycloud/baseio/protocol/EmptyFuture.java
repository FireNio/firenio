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
package com.generallycloud.baseio.protocol;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.EmptyByteBuf;
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.component.SocketChannelContext;

public class EmptyFuture extends AbstractChannelFuture {

    private static EmptyFuture EMPTY_FUTURE = null;

    public static void initializeReadFuture(SocketChannelContext context) {
        if (EMPTY_FUTURE != null) {
            return;
        }
        EMPTY_FUTURE = new EmptyFuture(context);
        EMPTY_FUTURE.setByteBuf(EmptyByteBuf.getInstance());
    }

    public static EmptyFuture get() {
        return EMPTY_FUTURE;
    }

    public EmptyFuture(SocketChannelContext context) {
        super(context);
    }

    @Override
    public void release() {

    }

    @Override
    public boolean isReleased() {
        return true;
    }

    @Override
    public void write(byte[] bytes, int off, int len) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(byte b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean read(SocketChannel channel, ByteBuf src) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChannelFuture duplicate() {
        return this;
    }

}
