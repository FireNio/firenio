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
package com.generallycloud.test.io.load.http11;

import java.io.IOException;

import com.generallycloud.baseio.codec.http11.ClientHTTPProtocolFactory;
import com.generallycloud.baseio.codec.http11.future.HttpFuture;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.connector.SocketChannelConnector;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.test.io.common.FutureFactory;
import com.generallycloud.test.test.ITestThread;
import com.generallycloud.test.test.ITestThreadHandle;

public class TestHttpLoadClient extends ITestThread {

    private SocketChannelConnector connector;

    private SocketSession          session;

    @Override
    public void run() {

        int time = getTime();

        for (int i = 0; i < time; i++) {

            HttpFuture future = FutureFactory.createHttpReadFuture(session, "/test");

            session.flush(future);
        }
    }

    @Override
    public void prepare() throws Exception {

        IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {
            @Override
            public void accept(SocketSession session, Future future) throws Exception {
                addCount(1000);
            }
        };

        ServerConfiguration c = new ServerConfiguration("localhost", 80);

        c.setSERVER_MEMORY_POOL_CAPACITY(1280000);
        c.setSERVER_MEMORY_POOL_UNIT(128);
        c.setSERVER_CORE_SIZE(1);
        c.setSERVER_HOST("192.168.0.180");

        SocketChannelContext context = new NioSocketChannelContext(c);

        connector = new SocketChannelConnector(context);

        context.setProtocolFactory(new ClientHTTPProtocolFactory());
        context.setIoEventHandleAdaptor(eventHandleAdaptor);
        context.addSessionEventListener(new LoggerSocketSEListener());

        session = connector.connect();
    }

    @Override
    public void stop() {
        CloseUtil.close(connector);
    }

    public static void main(String[] args) throws IOException {

        int time = 160 * 10000;

        int core_size = 4;

        ITestThreadHandle.doTest(TestHttpLoadClient.class, core_size, time / core_size);
    }

}
