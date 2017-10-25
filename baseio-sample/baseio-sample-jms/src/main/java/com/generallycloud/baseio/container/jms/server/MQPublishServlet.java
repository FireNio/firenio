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

import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.jms.Message;

public class MQPublishServlet extends MQServlet {

    public static final String SERVICE_NAME = MQPublishServlet.class.getSimpleName();

    @Override
    public void doAccept(SocketSession session, ProtobaseFuture future,
            MQSessionAttachment attachment) throws Exception {

        MQContext context = getMQContext();

        Message message = context.parse(future);

        context.publishMessage(message);

        future.write("1");

        session.flush(future);
    }
}
