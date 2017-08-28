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

import java.io.IOException;

import com.generallycloud.baseio.codec.fixedlength.FixedLengthProtocolFactory;
import com.generallycloud.baseio.codec.fixedlength.future.FixedLengthFuture;
import com.generallycloud.baseio.codec.fixedlength.future.FixedLengthFutureImpl;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.connector.SocketChannelConnector;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.test.test.ITestThread;
import com.generallycloud.test.test.ITestThreadHandle;

public class TestLoadClient1 extends ITestThread {

    private SocketChannelConnector connector = null;

    @Override
    public void run() {

        int time1 = getTime();

        SocketSession session = connector.getSession();

        for (int i = 0; i < time1; i++) {

            FixedLengthFuture future = new FixedLengthFutureImpl(session.getContext());

            future.write("hello server!");

            session.flush(future);
        }
    }

    @Override
    public void prepare() throws Exception {

        IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {
            @Override
            public void accept(SocketSession session, Future future) throws Exception {

                addCount(10000);
            }
        };

        ServerConfiguration configuration = new ServerConfiguration(8300);

        configuration.setSERVER_MEMORY_POOL_CAPACITY(1280000);
        configuration.setSERVER_MEMORY_POOL_UNIT(128);
        configuration.setSERVER_ENABLE_MEMORY_POOL_DIRECT(true);
        configuration.setSERVER_ENABLE_MEMORY_POOL(true);
        //		c.setSERVER_HOST("192.168.0.180");

        SocketChannelContext context = new NioSocketChannelContext(configuration);

        connector = new SocketChannelConnector(context);

        context.setIoEventHandleAdaptor(eventHandleAdaptor);
        context.addSessionEventListener(new LoggerSocketSEListener());
        context.setProtocolFactory(new FixedLengthProtocolFactory());

        connector.connect();
    }

    @Override
    public void stop() {
        CloseUtil.close(connector);
    }

    public static void main(String[] args) throws IOException {

        int time = 128 * 10000;

        int core_size = 4;

        ITestThreadHandle.doTest(TestLoadClient1.class, core_size, time / core_size);
    }
}
