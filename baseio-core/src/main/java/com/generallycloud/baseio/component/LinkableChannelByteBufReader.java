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
package com.generallycloud.baseio.component;

import com.generallycloud.baseio.buffer.ByteBuf;

public abstract class LinkableChannelByteBufReader implements ChannelByteBufReader {

    private ChannelByteBufReader next;

    @Override
    public ChannelByteBufReader getNext() {
        return next;
    }

    @Override
    public void setNext(ChannelByteBufReader channelByteBufReader) {
        this.next = channelByteBufReader;
    }

    protected ByteBuf allocate(SocketChannel channel, int capacity) {
        return channel.getByteBufAllocator().allocate(capacity);
    }

    protected void nextAccept(SocketChannel channel, ByteBuf buffer) throws Exception {
        getNext().accept(channel, buffer);
    }

}
