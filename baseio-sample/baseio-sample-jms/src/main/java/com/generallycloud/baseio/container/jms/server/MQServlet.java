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
package com.generallycloud.baseio.container.jms.server;

import com.generallycloud.baseio.codec.protobase.future.ParamedProtobaseFuture;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.container.protobase.ProtobaseFutureAcceptorService;

public abstract class MQServlet extends ProtobaseFutureAcceptorService {

    private MQContext context = MQContext.getInstance();

    public MQContext getMQContext() {
        return context;
    }

    @Override
    public void doAccept(NioSocketChannel channel, ParamedProtobaseFuture future) throws Exception {

        MQChannelAttachment attachment = context.getChannelAttachment(channel);

        this.doAccept(channel, future, attachment);
    }

    public abstract void doAccept(NioSocketChannel channel, ParamedProtobaseFuture future,
            MQChannelAttachment attachment) throws Exception;

}
