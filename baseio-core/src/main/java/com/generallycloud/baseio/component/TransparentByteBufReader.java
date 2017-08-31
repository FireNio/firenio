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
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.ProtocolDecoder;

public class TransparentByteBufReader extends LinkableChannelByteBufReader {

    private ForeFutureAcceptor foreReadFutureAcceptor;

    public TransparentByteBufReader(SocketChannelContext context) {
        this.foreReadFutureAcceptor = context.getForeReadFutureAcceptor();
    }

    @Override
    public void accept(SocketChannel channel, ByteBuf buf) throws Exception {

        for (;;) {

            if (!buf.hasRemaining()) {
                return;
            }

            ChannelFuture future = channel.getReadFuture();

            if (future == null) {

                ProtocolDecoder decoder = channel.getProtocolDecoder();

                future = decoder.decode(channel, buf);

                if (future == null) {
                    CloseUtil.close(channel);
                    return;
                }

                channel.setReadFuture(future);
            }

            try {

                if (!future.read(channel, buf)) {
                    return;
                }

                ReleaseUtil.release(future);

            } catch (Throwable e) {

                ReleaseUtil.release(future);

                if (e instanceof IOException) {
                    throw (IOException) e;
                }

                throw new IOException(
                        "exception occurred when read from channel,the nested exception is,"
                                + e.getMessage(),
                        e);
            }

            channel.setReadFuture(null);

            foreReadFutureAcceptor.accept(channel.getSession(), future);
        }
    }
}
