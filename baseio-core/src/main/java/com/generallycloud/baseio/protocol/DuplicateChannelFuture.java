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

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.component.ByteArrayBuffer;
import com.generallycloud.baseio.component.SocketChannelContext;

/**
 * @author wangkai
 *
 */
//FIXME add unsupported operation
public class DuplicateChannelFuture extends DefaultChannelFuture {

    private ChannelFuture prototype;

    public DuplicateChannelFuture(SocketChannelContext context, ByteBuf buf,
            ChannelFuture prototype) {
        super(context, buf);
        this.prototype = prototype;
    }

    private ChannelFuture unwrap() {
        return prototype;
    }

    @Override
    public ChannelFuture duplicate() {
        return unwrap().duplicate();
    }

    @Override
    public ByteArrayBuffer getWriteBuffer() {
        return unwrap().getWriteBuffer();
    }

    @Override
    public String getReadText() {
        return unwrap().getReadText();
    }

}
