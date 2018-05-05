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
import com.generallycloud.baseio.concurrent.Linkable;

public interface ChannelFuture extends Future, Linkable {

    ChannelFuture duplicate();

    ChannelFuture flush();

    ByteBuf getByteBuf();

    int getByteBufLimit();

    boolean isHeartbeat();
    
    boolean isNeedSsl();

    boolean isPING();

    boolean isPONG();
    
    boolean isSilent();

    boolean isWriteCompleted();

    /**
     * return true if the future read complete
     * @param channel
     * @param src
     * @return
     * @throws IOException
     */
    boolean read(SocketChannel channel, ByteBuf src) throws IOException;

    void setByteBuf(ByteBuf buf);

    void setHeartbeat(boolean isPing);
    
    void setNeedSsl(boolean needSsl);

    ChannelFuture setPING();

    ChannelFuture setPONG();

    void setSilent(boolean isSilent);

}
