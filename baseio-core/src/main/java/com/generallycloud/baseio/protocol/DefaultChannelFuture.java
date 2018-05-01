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

/**
 * @author wangkai
 *
 */
//FIXME 部分方法需要抛UnsupportedException
//FIXME .....部分使用该类地方是否可以使用EmptyFuture
public class DefaultChannelFuture extends AbstractChannelFuture {

    public DefaultChannelFuture(ByteBuf buf) {
        this.buf = buf;
    }
    
    public DefaultChannelFuture(ByteBuf buf,boolean isNeedSsl) {
        this.buf = buf;
        this.setNeedSsl(isNeedSsl);
    }

    @Override
    public boolean read(SocketChannel channel, ByteBuf src) throws IOException {
        throw new UnsupportedOperationException();
    }

}
