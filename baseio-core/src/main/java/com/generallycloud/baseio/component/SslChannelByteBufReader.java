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
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.protocol.SslFuture;
import com.generallycloud.baseio.protocol.SslFutureImpl;

public class SslChannelByteBufReader extends LinkableChannelByteBufReader {

    @Override
    public void accept(SocketChannel channel, ByteBuf buffer) throws Exception {

        for (;;) {

            if (!buffer.hasRemaining()) {
                return;
            }

            SslFuture future = channel.getSslReadFuture();

            if (future == null) {

                ByteBuf buf = allocate(channel, SslFuture.SSL_RECORD_HEADER_LENGTH);

                future = new SslFutureImpl(channel, buf, 1024 * 64);//FIXME param

                channel.setSslReadFuture(future);
            }

            try {

                if (!future.read(channel, buffer)) {

                    return;
                }

            } catch (Throwable e) {

                ReleaseUtil.release(future);

                channel.setSslReadFuture(null);

                if (e instanceof IOException) {
                    throw (IOException) e;
                }

                throw new IOException(
                        "exception occurred when read from channel,the nested exception is,"
                                + e.getMessage(),
                        e);
            }

            channel.setSslReadFuture(null);

            ByteBuf produce = future.getProduce();

            if (produce == null) {
                continue;
            }

            try {

                nextAccept(channel, produce);

            } finally {

                ReleaseUtil.release(future);
            }
        }
    }

}
