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

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.component.ssl.SslHandler;
import com.generallycloud.baseio.protocol.SslFuture;
import com.generallycloud.baseio.protocol.SslFutureImpl;

public class SslChannelByteBufReader extends LinkableChannelByteBufReader {

    private SslFuture temporary;

    public SslChannelByteBufReader() {
        //FIXME ..这里需要使用heap，使用direct总是到jvm崩溃，尚未找到原因
//        ByteBuf buf = UnpooledByteBufAllocator.getDirect().allocate(1024 * 64);
        ByteBuf buf = UnpooledByteBufAllocator.getHeap().allocate(1024 * 64);
        temporary = new SslFutureImpl(buf, 1024 * 64);
    }

    @Override
    public void accept(SocketChannel channel, ByteBuf buffer) throws Exception {
        for (;;) {
            if (!buffer.hasRemaining()) {
                return;
            }
            SslFuture future = channel.getSslReadFuture();
            boolean setFutureNull = true;
            if (future == null) {
                future = this.temporary.reset();
                setFutureNull = false;
            }
            try {
                if (!future.read(channel, buffer)) {
                    if (!setFutureNull) {
                        if (future == this.temporary) {
                            future = future.copy(channel);
                        }
                        channel.setSslReadFuture(future);
                    }
                    return;
                }
            } catch (Throwable e) {
                ReleaseUtil.release(future,channel.getChannelThreadContext());
                channel.setSslReadFuture(null);
                if (e instanceof IOException) {
                    throw (IOException) e;
                }
                throw new IOException("exception occurred when do decode ssl," + e.getMessage(), e);
            }
            if (setFutureNull) {
                channel.setSslReadFuture(null);
            }
            SslHandler sslHandler = channel.getSslHandler();
            ByteBuf product;
            try {
                product = sslHandler.unwrap(channel, future.getByteBuf());
            } finally {
                ReleaseUtil.release(future,channel.getChannelThreadContext());
            }
            if (product == null) {
                continue;
            }
            nextAccept(channel, product);
        }
    }

}
