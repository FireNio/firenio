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
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.concurrent.Linkable;

public interface ChannelFuture extends Future, Linkable {

    ChannelFuture flush();

    boolean isHeartbeat();

    boolean isPING();

    boolean isPONG();

    boolean read(SocketChannel channel, ByteBuf src) throws IOException;

    ChannelFuture setPING();

    ChannelFuture setPONG();

    boolean isSilent();

    void setSilent(boolean isSilent);

    void write(SocketChannel channel) throws IOException;

    boolean isWriteCompleted();

    ByteBuf getByteBuf();

    ChannelFuture duplicate();

    void onException(SocketSession session, Exception e);

    void onSuccess(SocketSession session);

    int getByteBufLimit();

    void setByteBuf(ByteBuf buf);

}
