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
package com.generallycloud.test.io.fixedlength;

import java.io.IOException;

import com.generallycloud.baseio.codec.fixedlength.FixedLengthCodec;
import com.generallycloud.baseio.codec.fixedlength.FixedLengthFuture;
import com.generallycloud.baseio.codec.fixedlength.FixedLengthFutureImpl;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.ChannelAcceptor;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.configuration.Configuration;
import com.generallycloud.baseio.protocol.Future;

public class SimpleTestFIxedLengthBroadcastServer {

    public static void main(String[] args) throws Exception {

        IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

            @Override
            public void accept(SocketSession session, Future future) throws Exception {
                FixedLengthFuture f = (FixedLengthFuture) future;
                future.write("yes server already accept your message:", session);
                future.write(f.getReadText(), session);
                session.flush(f);
            }
        };

        ChannelContext context = new ChannelContext(new Configuration(8300));
        ChannelAcceptor acceptor = new ChannelAcceptor(context);
        context.addSessionEventListener(new LoggerSocketSEListener());
        context.addSessionEventListener(new SetOptionListener());
        context.setIoEventHandleAdaptor(eventHandleAdaptor);
        context.setProtocolCodec(new FixedLengthCodec());
        acceptor.bind();

        ThreadUtil.exec(new Runnable() {

            @Override
            public void run() {
                for (;;) {
                    ThreadUtil.sleep(1000);
                    FixedLengthFuture future = new FixedLengthFutureImpl();
                    future.write("broadcast msg .........................", context);
                    try {
                        acceptor.broadcast(future);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
