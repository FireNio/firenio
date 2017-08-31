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

import com.generallycloud.baseio.balance.BalanceFuture;
import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketChannel;

/**
 *
 */
public class SessionIdProtobaseBinaryFutureImpl extends ProtobaseBinaryFutureImpl
        implements SessionIdProtobaseFuture {

    public SessionIdProtobaseBinaryFutureImpl(SocketChannelContext context) {
        super(context);
    }

    public SessionIdProtobaseBinaryFutureImpl(SocketChannel channel, ByteBuf buf, int binaryLimit,
            boolean isBroadcast) {
        super(channel, buf, binaryLimit);
        this.isBroadcast = isBroadcast;
    }

    private int     sessionId;

    private boolean isBroadcast;

    @Override
    public Object getSessionKey() {
        return getSessionId();
    }

    @Override
    public boolean isBroadcast() {
        return isBroadcast;
    }

    @Override
    public void setBroadcast(boolean broadcast) {
        this.isBroadcast = broadcast;
    }

    @Override
    public BalanceFuture translate() {
        String text = getReadText();
        if (!StringUtil.isNullOrBlank(text)) {
            write(text);
        }
        byte[] binary = getBinary();
        if (binary == null) {
            return this;
        }
        writeBinary(binary);
        return this;
    }

    @Override
    public int getSessionId() {
        return sessionId;
    }

    @Override
    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    protected void generateHeaderExtend(ByteBuf buf) {
        this.sessionId = buf.getInt();
    }

}
