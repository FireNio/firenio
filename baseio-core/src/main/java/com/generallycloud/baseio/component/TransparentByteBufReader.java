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
import com.generallycloud.baseio.protocol.ChannelFuture;

public class TransparentByteBufReader extends LinkableChannelByteBufReader {

    private ForeFutureAcceptor foreReadFutureAcceptor;
    
    public TransparentByteBufReader(SocketChannelContext context) {
        this.foreReadFutureAcceptor = context.getForeReadFutureAcceptor();
    }

    @Override
    public void accept(SocketChannel channel, ByteBuf buffer) throws Exception {
        for (;;) {
            if (!buffer.hasRemaining()) {
                return;
            }
            ChannelFuture future = channel.getReadFuture();
            boolean setFutureNull = true;
            if (future == null) {
                future = channel.getProtocolCodec().decode(channel, buffer);
                setFutureNull = false;
            }
            try {
                if (!future.read(channel, buffer)) {
                    if (!setFutureNull) {
                        channel.setReadFuture(future);
                    }
                    return;
                }
            } catch (Throwable e) {
                ReleaseUtil.release(future);
                if (e instanceof IOException) {
                    throw (IOException) e;
                }
                throw new IOException("exception occurred when do decode," + e.getMessage(), e);
            }
            if (setFutureNull) {
                channel.setReadFuture(null);
            }
            ReleaseUtil.release(future);
            foreReadFutureAcceptor.accept(channel.getSession(), future);
        }
    }
}
