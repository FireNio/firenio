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
import com.generallycloud.baseio.component.JsonParameters;
import com.generallycloud.baseio.component.Parameters;
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.component.SocketChannelContext;

/**
 * @author wangkai
 *
 */
public class ParamedProtobaseFutureImpl extends ProtobaseFutureImpl implements ParamedProtobaseFuture{

    public ParamedProtobaseFutureImpl(SocketChannel channel, ByteBuf buf) {
        super(channel, buf);
    }

    // for ping & pong
    public ParamedProtobaseFutureImpl(SocketChannelContext context) {
        super(context);
    }

    public ParamedProtobaseFutureImpl(SocketChannelContext context, String futureName) {
        super(context, futureName);
    }

    public ParamedProtobaseFutureImpl(SocketChannelContext context, int futureId, String futureName) {
        super(context, futureId, futureName);
    }

    private Parameters parameters;

    public Parameters getParameters() {
        if (parameters == null) {
            parameters = new JsonParameters(getReadText());
        }
        return parameters;
    }

}
