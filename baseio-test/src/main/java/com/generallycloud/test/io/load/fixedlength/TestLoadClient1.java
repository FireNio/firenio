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

import com.generallycloud.baseio.codec.fixedlength.FixedLengthFuture;
import com.generallycloud.baseio.codec.fixedlength.FixedLengthFutureImpl;
import com.generallycloud.baseio.codec.fixedlength.FixedLengthCodec;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.configuration.Configuration;
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
            FixedLengthFuture future = new FixedLengthFutureImpl();
            future.write("hello server!",session);
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

        Configuration configuration = new Configuration(8300);

        configuration.setMemoryPoolCapacity(320000);
        configuration.setMemoryPoolUnit(128);
        configuration.setEnableMemoryPoolDirect(true);
        configuration.setEnableMemoryPool(true);

        SocketChannelContext context = new NioSocketChannelContext(configuration);

        connector = new SocketChannelConnector(context);

        context.setIoEventHandleAdaptor(eventHandleAdaptor);
        context.addSessionEventListener(new LoggerSocketSEListener());
        context.setProtocolCodec(new FixedLengthCodec());

        connector.connect();
    }

    @Override
    public void stop() {
        CloseUtil.close(connector);
    }

    public static void main(String[] args) throws IOException {

        int time = 128 * 10000;

        int core_size = 16;

        ITestThreadHandle.doTest(TestLoadClient1.class, core_size, time / core_size);
    }
}
