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
package com.generallycloud.test.io.load.fixedlength;

import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.baseio.acceptor.SocketChannelAcceptor;
import com.generallycloud.baseio.codec.fixedlength.FixedLengthProtocolFactory;
import com.generallycloud.baseio.codec.fixedlength.future.FixedLengthFuture;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.Future;

public class TestLoadServer {

    public static void main(String[] args) throws Exception {

        LoggerFactory.configure();

        final AtomicInteger res = new AtomicInteger();
        final AtomicInteger req = new AtomicInteger();

        IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

            @Override
            public void accept(SocketSession session, Future future) throws Exception {
                FixedLengthFuture f = (FixedLengthFuture) future;
                String res = "yes server already accept your message" + f.getReadText();
                f.write(res);
                session.flush(future);
                //				System.out.println("req======================"+req.getAndIncrement());
            }

            @Override
            public void futureSent(SocketSession session, Future future) {
                //				NIOReadFuture f = (NIOReadFuture) future;
                //				System.out.println(f.getWriteBuffer());
                //				System.out.println("res==========="+res.getAndIncrement());
            }
        };

        ServerConfiguration c = new ServerConfiguration(8300);

        c.setSERVER_MEMORY_POOL_CAPACITY(2560000);
        c.setSERVER_MEMORY_POOL_UNIT(128);
        c.setSERVER_MEMORY_POOL_CAPACITY_RATE(0.5);
        c.setSERVER_ENABLE_MEMORY_POOL_DIRECT(true);
        c.setSERVER_CORE_SIZE(4);

        SocketChannelContext context = new NioSocketChannelContext(c);

        SocketChannelAcceptor acceptor = new SocketChannelAcceptor(context);

        context.setProtocolFactory(new FixedLengthProtocolFactory());

        context.setIoEventHandleAdaptor(eventHandleAdaptor);

        context.addSessionEventListener(new LoggerSocketSEListener());

        //		context.addSessionEventListener(new SetOptionListener());

        acceptor.bind();
    }
}
