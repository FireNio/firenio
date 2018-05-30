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
package com.generallycloud.test.io.protobase;

import com.generallycloud.baseio.codec.protobase.ProtobaseCodec;
import com.generallycloud.baseio.codec.protobase.ProtobaseFuture;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.configuration.Configuration;
import com.generallycloud.baseio.container.protobase.FixedSession;
import com.generallycloud.baseio.container.protobase.OnFuture;
import com.generallycloud.baseio.container.protobase.SimpleIoEventHandle;
import com.generallycloud.baseio.protocol.Future;

public class TestSessionDisconnect {

    public static void main(String[] args) throws Exception {

        String serviceName = "TestSessionDisconnectServlet";
        String param = "ttt";
        SimpleIoEventHandle eventHandle = new SimpleIoEventHandle();
        Configuration configuration = new Configuration(8300);
        ChannelContext context = new ChannelContext(configuration);
        ChannelConnector connector = new ChannelConnector(context);
        context.setIoEventHandleAdaptor(eventHandle);
        context.setProtocolCodec(new ProtobaseCodec());
        context.addSessionEventListener(new LoggerSocketSEListener());
        FixedSession session = new FixedSession(connector.connect());
        ProtobaseFuture future = session.request(serviceName, param);
        System.out.println(future.getReadText());
        session.listen(serviceName, new OnFuture() {
            @Override
            public void onResponse(SocketSession session, Future future) {
                ProtobaseFuture f = (ProtobaseFuture) future;
                System.out.println(f.getReadText());
            }
        });
        session.write(serviceName, param);
        ThreadUtil.sleep(9999);
        CloseUtil.close(connector);
    }

}
