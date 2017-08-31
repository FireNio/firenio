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
package com.generallycloud.baseio.codec.charbased.future;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.component.ByteArrayBuffer;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.protocol.AbstractChannelFuture;

public class CharBasedFutureImpl extends AbstractChannelFuture implements CharBasedFuture {

    private boolean         complete;

    private int             limit;

    private byte            splitor;

    private ByteArrayBuffer cache = new ByteArrayBuffer();

    public CharBasedFutureImpl(SocketChannelContext context, int limit, byte splitor) {
        super(context);
        this.limit = limit;
        this.splitor = splitor;
    }

    public CharBasedFutureImpl(SocketChannelContext context) {
        super(context);
    }

    private void doBodyComplete() {

        this.readText = cache.toString(context.getEncoding());

        this.complete = true;
    }

    @Override
    public boolean read(SocketChannel channel, ByteBuf buffer) throws IOException {

        if (complete) {
            return true;
        }

        ByteArrayBuffer cache = this.cache;

        for (; buffer.hasRemaining();) {

            byte b = buffer.getByte();

            if (b == splitor) {
                doBodyComplete();
                return true;
            }

            cache.write(b);

            if (cache.size() > limit) {
                throw new IOException("max length " + limit);
            }
        }

        return false;
    }

    @Override
    public ByteArrayBuffer getLineOutputStream() {
        return cache;
    }

}
