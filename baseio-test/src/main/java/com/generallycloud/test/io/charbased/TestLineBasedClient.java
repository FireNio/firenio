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
package com.generallycloud.test.io.charbased;

import com.generallycloud.baseio.codec.charbased.CharBasedCodec;
import com.generallycloud.baseio.codec.charbased.CharBasedFuture;
import com.generallycloud.baseio.codec.charbased.CharBasedFutureImpl;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.configuration.Configuration;
import com.generallycloud.baseio.protocol.Future;

public class TestLineBasedClient {

    public static void main(String[] args) throws Exception {

        IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

            @Override
            public void accept(SocketSession session, Future future) throws Exception {
                System.out.println();
                System.out.println("____________________" + future);
                System.out.println();
            }
        };

        ChannelContext context = new ChannelContext(new Configuration(8300));
        ChannelConnector connector = new ChannelConnector(context);
        context.setIoEventHandle(eventHandleAdaptor);
        context.addSessionEventListener(new LoggerSocketSEListener());
        context.setProtocolCodec(new CharBasedCodec());
        SocketSession session = connector.connect();
        CharBasedFuture future = new CharBasedFutureImpl();
        future.write("hello server!", session);
        session.flush(future);
        ThreadUtil.sleep(100);
        CloseUtil.close(connector);

    }
}
