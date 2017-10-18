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
package com.generallycloud.baseio.codec.protobase.future;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketChannel;

/**
 *
 */
public class HashedProtobaseFutureImpl extends SessionIdProtobaseFutureImpl
        implements HashedProtobaseFuture {

    public HashedProtobaseFutureImpl(SocketChannelContext context) {
        super(context);
    }

    public HashedProtobaseFutureImpl(SocketChannelContext context, int futureId,
            String futureName) {
        super(context, futureId, futureName);
    }

    public HashedProtobaseFutureImpl(SocketChannelContext context, String futureName) {
        this(context, 0, futureName);
    }

    public HashedProtobaseFutureImpl(SocketChannel channel, ByteBuf buf, boolean isBroadcast) {
        super(channel, buf, isBroadcast);
    }

    private int hashCode;

    @Override
    public void setHashCode(int hashCode) {
        this.hashCode = hashCode;
    }

    @Override
    public int getHashCode() {
        return hashCode;
    }

    @Override
    protected void generateHeaderExtend(ByteBuf buf) {
        super.generateHeaderExtend(buf);
        this.hashCode = buf.getInt();
    }

}
